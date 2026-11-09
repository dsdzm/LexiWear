"""构建 App 内置词库与教材词表资源（开发期脚本，运行结果已提交到 assets/）

用法:
    python3 tools/build_assets.py --source data/final_data.json --out app/src/main/assets
"""
import argparse, gzip, json, os, re, time, threading
from concurrent.futures import ThreadPoolExecutor, as_completed
import requests

DICT_PARAM = json.dumps(
    {'count': 5, 'dicts': [['ec'], ['blng_sents_part'], ['rel_word'], ['web_trans']]},
    separators=(',', ':'))
TAG = re.compile(r'<[^>]+>')
_local = threading.local()


def sess():
    if not hasattr(_local, 's'):
        _local.s = requests.Session()
        _local.s.headers.update({'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 '
                                              '(KHTML, like Gecko) Chrome/120 Safari/537.36'})
    return _local.s


def clean(s):
    return re.sub(r'\s+', ' ', TAG.sub('', s or '')).strip()


def fetch(word, tries=3):
    for i in range(tries):
        try:
            r = sess().get('https://dict.youdao.com/jsonapi',
                           params={'q': word, 'dicts': DICT_PARAM}, timeout=18)
            if r.status_code == 200:
                return r.json()
        except Exception:
            pass
        time.sleep(0.4 * (i + 1))
    return {}


def parse(word, d, fallback=None):
    fallback = fallback or {}
    out = {'w': word, 'uk': '', 'us': '', 'p': fallback.get('pos', ''),
           's': [], 'f': [], 'r': [], 'e': []}
    ec = d.get('ec') or {}
    ew = (ec.get('word') or [{}])[0]
    out['uk'] = clean(ew.get('ukphone', '')).strip('[]/ ')
    out['us'] = clean(ew.get('usphone', '')).strip('[]/ ')
    senses = []
    for t in (ew.get('trs') or []):
        for tr in (t.get('tr') or []):
            for i in (tr.get('l', {}).get('i') or []):
                x = clean(i)
                if x and x not in senses:
                    senses.append(x)
    out['s'] = senses[:4]
    forms = []
    for f in (ew.get('wfs') or []):
        wf = f.get('wf') or {}
        if wf.get('name') and wf.get('value'):
            forms.append({'n': clean(wf['name']), 'v': clean(wf['value'])})
    out['f'] = forms[:6]
    rel = []
    rw = d.get('rel_word') or {}
    for group in (rw.get('rels') or []):
        r0 = group.get('rel') or {}
        pos = clean(r0.get('pos', ''))
        for x in (r0.get('words') or []):
            w = clean(x.get('word', ''))
            t = clean(x.get('tran', ''))
            if w and w.lower() != word.lower() and len(rel) < 10:
                rel.append({'p': pos, 'w': w, 't': t[:60]})
    out['r'] = rel
    ex = []
    bp = (d.get('blng_sents_part') or {}).get('sentence-pair') or []
    for pair in bp[:4]:
        en = clean(pair.get('sentence', ''))
        zh = clean(pair.get('sentence-translation', ''))
        if en and zh:
            ex.append({'en': en[:220], 'zh': zh[:140]})
    out['e'] = ex[:3]
    if not out['s'] and fallback.get('meaning'):
        out['s'] = [fallback['meaning'][:160]]
    if not out['uk'] and fallback.get('uk'):
        out['uk'] = fallback['uk']
    if not out['us'] and fallback.get('us'):
        out['us'] = fallback['us']
    if not out['p']:
        out['p'] = ''
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--source', required=True)
    ap.add_argument('--out', required=True)
    ap.add_argument('--cache', default='.cache/lex.json')
    args = ap.parse_args()

    data = json.load(open(args.source, encoding='utf-8'))
    rows, toc = data['rows'], data['toc']

    # ---------- 教材结构 ----------
    versions, ver_idx, books, book_idx = [], {}, [], {}
    for t in toc:
        if t['version'] not in ver_idx:
            ver_idx[t['version']] = len(versions)
            versions.append({'id': f'v{len(versions)}', 'name': t['version'],
                             'group': t['group'], 'press': t['press'], 'books': []})
        vk = (t['version'], t['book'])
        if vk not in book_idx:
            bid = f'b{len(books)}'
            book_idx[vk] = len(books)
            books.append({'id': bid, 'v': ver_idx[t['version']], 'name': t['book'],
                          'grade': t['grade'], 'units': []})
            versions[ver_idx[t['version']]]['books'].append(bid)
        books[book_idx[vk]]['units'].append({'name': t['unit'], 'n': t['count']})

    # 每册单元内单词（用词条顺序）
    book_units = {}
    for r in rows:
        bid = books[book_idx[(r['version'], r['book'])]]['id']
        book_units.setdefault(bid, {}).setdefault(r['unit'], []).append(
            {'w': r['word'], 'p': r['pos'], 'm': r['meaning'][:80]})

    # ---------- 反查：搭配与教材出现位置 ----------
    phrases = [r for r in rows if r['type'] == '短语/词组' and 1 <= len(r['word'].split()) <= 4]
    phrase_map = {}
    for r in phrases:
        for tok in set(re.findall(r"[a-zA-Z][a-zA-Z'-]*", r['word'].lower())):
            phrase_map.setdefault(tok, [])
            if len(phrase_map[tok]) < 25:
                phrase_map[tok].append({'p': r['word'], 'm': r['meaning'][:60]})

    occur = {}
    for r in rows:
        k = r['word']
        item = {'v': r['version'], 'b': r['book'], 'u': r['unit']}
        lst = occur.setdefault(k, [])
        if item not in lst and len(lst) < 6:
            lst.append(item)

    meta = {}
    for r in rows:
        if r['word'] not in meta:
            meta[r['word']] = {'pos': r['pos'], 'meaning': r['meaning'],
                               'uk': r['uk'], 'us': r['us']}

    words = sorted(meta.keys())
    os.makedirs(os.path.dirname(args.cache) or '.', exist_ok=True)
    cache = {}
    if os.path.exists(args.cache):
        cache = json.load(open(args.cache, encoding='utf-8'))
    todo = [w for w in words if w not in cache]
    print(f'words={len(words)} cached={len(cache)} todo={len(todo)}', flush=True)
    t0 = time.time()
    with ThreadPoolExecutor(max_workers=16) as pool:
        futs = {pool.submit(fetch, w): w for w in todo}
        done = 0
        for f in as_completed(futs):
            w = futs[f]
            cache[w] = parse(w, f.result(), meta.get(w))
            done += 1
            if done % 2000 == 0:
                print(f'  {done}/{len(todo)} {time.time()-t0:.0f}s', flush=True)
                json.dump(cache, open(args.cache, 'w', encoding='utf-8'), ensure_ascii=False)
    json.dump(cache, open(args.cache, 'w', encoding='utf-8'), ensure_ascii=False)

    # ---------- 派生词补全（同根词不在主词表时也抓一遍） ----------
    extra = sorted({x['w'].lower() for w in cache.values() for x in w.get('r', [])
                    if x['w'] and ' ' not in x['w'] and len(x['w']) <= 30
                    and x['w'].lower() not in cache})
    print('extra rel words:', len(extra), flush=True)
    with ThreadPoolExecutor(max_workers=16) as pool:
        futs = {pool.submit(fetch, w): w for w in extra}
        for f in as_completed(futs):
            w = futs[f]
            cache[w] = parse(w, f.result(), {'meaning': ''})
    print('total lexicon entries:', len(cache), flush=True)

    # ---------- 写 assets ----------
    os.makedirs(os.path.join(args.out, 'lex'), exist_ok=True)
    os.makedirs(os.path.join(args.out, 'tb'), exist_ok=True)

    index, shards = [], {}
    for w, e in cache.items():
        brief = (e['s'][0] if e['s'] else '')[:46]
        index.append({'w': w, 'b': brief, 'p': e.get('p', '')})
        key = w[0].lower() if re.match(r'[a-z]', w[:1], re.I) else '_'
        shards.setdefault(key, {})[w] = e
    index.sort(key=lambda x: x['w'].lower())

    # 后缀用 .json.gzb 而非 .json.gz：aapt 会自动解压 *.gz 并去掉该后缀，导致运行时找不到文件
    def dump_gz(obj, path):
        raw = json.dumps(obj, ensure_ascii=False, separators=(',', ':')).encode()
        with gzip.open(path, 'wb', compresslevel=9) as fh:
            fh.write(raw)
        return len(raw)

    raw_total = dump_gz(index, os.path.join(args.out, 'lex', 'index.json.gzb'))
    for k, m in shards.items():
        raw_total += dump_gz(m, os.path.join(args.out, 'lex', f'{k}.json.gzb'))

    for b in books:
        units = [{'name': u['name'], 'words': book_units.get(b['id'], {}).get(u['name'], [])}
                 for u in b['units']]
        dump_gz({'id': b['id'], 'name': b['name'], 'grade': b['grade'],
                 'version': versions[b['v']]['name'], 'units': units},
                os.path.join(args.out, 'tb', f'{b["id"]}.json.gzb'))

    for i, v in enumerate(versions):
        v['books'] = [{'id': b['id'], 'name': b['name'], 'grade': b['grade'],
                       'units': len(b['units'])} for b in books if b['v'] == i]
    dump_gz({'versions': versions}, os.path.join(args.out, 'tb', 'index.json.gzb'))
    json.dump({'builtAt': time.strftime('%Y-%m-%d'), 'entries': len(cache),
               'words': len(words), 'versions': len(versions), 'books': len(books),
               'units': len(toc), 'rawBytes': raw_total},
              open(os.path.join(args.out, 'meta.json'), 'w', encoding='utf-8'),
              ensure_ascii=False, indent=1)
    print('assets written, raw bytes', raw_total, flush=True)

    # 补充：搭配与出现位置写回词条（体积考虑，单独小文件）
    enrich = {}
    for w in cache:
        low = w.lower()
        item = {}
        if low in phrase_map:
            item['c'] = phrase_map[low][:3]
        if w in occur:
            item['o'] = occur[w][:5]
        if item:
            enrich[w] = item
    dump_gz(enrich, os.path.join(args.out, 'lex', 'enrich.json.gzb'))
    print('enrich entries', len(enrich), flush=True)


if __name__ == '__main__':
    main()
