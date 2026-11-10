#!/usr/bin/env python3
"""Render a markdown file to HTML (scrolling tables, readable columns) and open it in the default browser."""
import argparse, hashlib, html, os, re, subprocess, sys, tempfile
import markdown

HERE = os.path.dirname(os.path.abspath(__file__))


def main():
    ap = argparse.ArgumentParser(prog="mdview", description=__doc__)
    ap.add_argument("file", help="markdown file")
    ap.add_argument("--no-open", action="store_true", help="only write the HTML and print its path")
    ap.add_argument("--css", help="extra CSS file appended after the built-in style")
    ap.add_argument("-o", "--out", help="output HTML path (default: a temp file)")
    a = ap.parse_args()

    src_path = os.path.abspath(a.file)
    if not os.path.isfile(src_path):
        sys.exit(f"mdview: not a file: {a.file}")
    text = open(src_path, encoding="utf-8", errors="replace").read()

    fm_html = ""
    m = re.match(r"---[ \t]*\n(.*?)\n---[ \t]*\n", text, re.S)
    if m:
        fm_html = f'<details class="fm"><summary>front matter</summary><pre>{html.escape(m.group(1))}</pre></details>'
        text = text[m.end():]

    body = markdown.markdown(
        text, extensions=["tables", "fenced_code", "sane_lists", "toc", "codehilite"],
        extension_configs={"codehilite": {"guess_lang": False, "noclasses": True}},
    )
    css = open(os.path.join(HERE, "style.css"), encoding="utf-8").read()
    if a.css:
        css += "\n" + open(a.css, encoding="utf-8").read()
    title = html.escape(os.path.basename(src_path))
    base = "file://" + os.path.dirname(src_path).replace(" ", "%20") + "/"
    page = (f'<!doctype html><html><head><meta charset="utf-8"><title>{title}</title>'
            f'<base href="{base}"><style>{css}</style></head><body>{fm_html}{body}</body></html>')

    out = a.out
    if not out:
        d = os.path.join(os.environ.get("XDG_RUNTIME_DIR") or tempfile.gettempdir(), "mdview")
        os.makedirs(d, exist_ok=True)
        h = hashlib.sha1(src_path.encode()).hexdigest()[:8]
        out = os.path.join(d, f"{os.path.splitext(os.path.basename(src_path))[0]}-{h}.html")
    open(out, "w", encoding="utf-8").write(page)
    print(out)
    if not a.no_open:
        subprocess.Popen(["xdg-open", out], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, start_new_session=True)


if __name__ == "__main__":
    main()
