#!/usr/bin/env python3
"""把本地最后一次提交同步到 GitHub（github.com:443 不可达时走 REST API）。

- 读取远程 main 的 tree 作为 base
- 重命名（R）复用旧 blob 的 sha，不重复上传
- 修改/新增（M/A）上传新 blob
- 删除（D）在 tree 中置 sha=null
"""
import base64
import json
import os
import subprocess
import sys

import requests

API = "https://api.github.com"
REPO = "dsdzm/LexiWear"
ROOT = "/root/.codebuddy/artifact/2f8a7d30-9c21-4f0b-1d6a-c7fbe5cf1000/lexiwear"

TOKEN = open("/root/.gh_token").read().strip()
S = requests.Session()
S.headers.update({"Authorization": f"token {TOKEN}",
                  "Accept": "application/vnd.github+json"})


def changed():
    out = subprocess.run(["git", "diff", "--name-status", "-M", "HEAD~1", "HEAD"],
                         cwd=ROOT, capture_output=True, text=True, check=True).stdout
    rows = []
    for line in out.strip().split("\n"):
        if not line.strip():
            continue
        parts = line.split("\t")
        rows.append(parts)
    return rows


def main():
    msg = subprocess.run(["git", "log", "-1", "--pretty=%B"], cwd=ROOT,
                         capture_output=True, text=True, check=True).stdout.strip()

    ref = S.get(f"{API}/repos/{REPO}/git/ref/heads/main", timeout=30).json()
    base_sha = ref["object"]["sha"]
    base_tree = S.get(f"{API}/repos/{REPO}/git/commits/{base_sha}", timeout=30).json()["tree"]["sha"]

    tree_map = {}
    tr = S.get(f"{API}/repos/{REPO}/git/trees/{base_tree}?recursive=1", timeout=60).json()
    for node in tr.get("tree", []):
        if node["type"] == "blob":
            tree_map[node["path"]] = node["sha"]
    print("remote blobs", len(tree_map), "base", base_sha[:8])

    entries = []
    blob_ok = renamed = deleted = 0

    def upload(path):
        raw = open(os.path.join(ROOT, path), "rb").read()
        r = S.post(f"{API}/repos/{REPO}/git/blobs",
                   data=json.dumps({"content": base64.b64encode(raw).decode(),
                                    "encoding": "base64"}), timeout=120)
        if r.status_code not in (200, 201):
            print("BLOB FAIL", path, r.status_code, r.text[:200])
            sys.exit(1)
        return r.json()["sha"]

    for parts in changed():
        st = parts[0]
        if st.startswith("R"):
            old, new = parts[1], parts[2]
            sha = tree_map.get(old) or upload(new)
            entries.append({"path": new, "mode": "100644", "type": "blob", "sha": sha})
            entries.append({"path": old, "mode": "100644", "type": "blob", "sha": None})
            renamed += 1
        elif st == "D":
            entries.append({"path": parts[1], "mode": "100644", "type": "blob", "sha": None})
            deleted += 1
        else:  # A / M / T
            sha = upload(parts[1])
            entries.append({"path": parts[1], "mode": "100644", "type": "blob", "sha": sha})
            blob_ok += 1
            print("  blob", parts[1])

    print(f"blobs={blob_ok} renamed={renamed} deleted={deleted}")

    t = S.post(f"{API}/repos/{REPO}/git/trees",
               data=json.dumps({"base_tree": base_tree, "tree": entries}), timeout=120)
    if t.status_code not in (200, 201):
        print("TREE FAIL", t.status_code, t.text[:300])
        sys.exit(1)
    new_tree = t.json()["sha"]

    c = S.post(f"{API}/repos/{REPO}/git/commits",
               data=json.dumps({"message": msg, "tree": new_tree, "parents": [base_sha]}),
               timeout=60)
    if c.status_code not in (200, 201):
        print("COMMIT FAIL", c.status_code, c.text[:300])
        sys.exit(1)
    new_commit = c.json()["sha"]

    u = S.patch(f"{API}/repos/{REPO}/git/refs/heads/main",
                data=json.dumps({"sha": new_commit, "force": False}), timeout=60)
    print("ref", u.status_code, u.json().get("object", {}).get("sha", "")[:8])
    if u.status_code not in (200, 201):
        print(u.text[:300])
        sys.exit(1)
    print("OK", new_commit)


if __name__ == "__main__":
    main()
