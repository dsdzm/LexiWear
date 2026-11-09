#!/usr/bin/env python3
"""通过 GitHub REST API 提交文件（github.com:443 不可达时使用）。"""
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
S.headers.update({
    "Authorization": f"token {TOKEN}",
    "Accept": "application/vnd.github+json",
})


def files_from_last_commit():
    out = subprocess.run(
        ["git", "diff", "--name-status", "HEAD~1", "HEAD"],
        cwd=ROOT, capture_output=True, text=True, check=True).stdout.strip().split("\n")
    res = []
    for line in out:
        status, path = line.split("\t", 1)
        res.append((status, path))
    return res


def main():
    commit_msg = subprocess.run(
        ["git", "log", "-1", "--pretty=%B"], cwd=ROOT,
        capture_output=True, text=True, check=True).stdout.strip()

    ref = S.get(f"{API}/repos/{REPO}/git/ref/heads/main", timeout=30).json()
    base_sha = ref["object"]["sha"]
    base_commit = S.get(f"{API}/repos/{REPO}/git/commits/{base_sha}", timeout=30).json()
    base_tree = base_commit["tree"]["sha"]
    print("base commit", base_sha[:8])

    entries = []
    for status, path in files_from_last_commit():
        full = os.path.join(ROOT, path)
        raw = open(full, "rb").read()
        blob = S.post(f"{API}/repos/{REPO}/git/blobs",
                      data=json.dumps({
                          "content": base64.b64encode(raw).decode(),
                          "encoding": "base64",
                      }), timeout=60)
        if blob.status_code not in (200, 201):
            print("BLOB FAIL", path, blob.status_code, blob.text[:200])
            sys.exit(1)
        entries.append({"path": path, "mode": "100644", "type": "blob",
                        "sha": blob.json()["sha"]})
        print(f"  blob {path} ({len(raw)}B)")

    tree = S.post(f"{API}/repos/{REPO}/git/trees",
                  data=json.dumps({"base_tree": base_tree, "tree": entries}), timeout=60)
    if tree.status_code not in (200, 201):
        print("TREE FAIL", tree.status_code, tree.text[:300])
        sys.exit(1)
    new_tree = tree.json()["sha"]
    print("new tree", new_tree[:8])

    commit = S.post(f"{API}/repos/{REPO}/git/commits",
                    data=json.dumps({"message": commit_msg,
                                     "tree": new_tree,
                                     "parents": [base_sha]}), timeout=60)
    if commit.status_code not in (200, 201):
        print("COMMIT FAIL", commit.status_code, commit.text[:300])
        sys.exit(1)
    new_commit = commit.json()["sha"]
    print("new commit", new_commit[:8])

    upd = S.patch(f"{API}/repos/{REPO}/git/refs/heads/main",
                  data=json.dumps({"sha": new_commit, "force": False}), timeout=60)
    print("ref update", upd.status_code, upd.json().get("object", {}).get("sha", "")[:8])
    if upd.status_code not in (200, 201):
        print(upd.text[:300])
        sys.exit(1)
    print("OK", new_commit)


if __name__ == "__main__":
    main()
