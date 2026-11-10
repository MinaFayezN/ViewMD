from markdown_it import MarkdownIt

css = open("src/main/resources/web/viewmd.css", "r", encoding="utf-8").read()
js = open("src/main/resources/web/postprocess.js", "r", encoding="utf-8").read()

md = MarkdownIt("gfm-like")

for name in ["doc-200kb", "doc-1mb"]:
    src_file = f"web-test/fixtures/{name}.md"
    out_file = f"web-test/fixtures/{name}.html"
    with open(src_file, "r", encoding="utf-8") as f:
        text = f.read()
    rendered = md.render(text)
    html = f"""<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <style>{css}</style>
</head>
<body>
  {rendered}
  <script>{js}</script>
</body>
</html>"""
    with open(out_file, "w", encoding="utf-8") as f:
        f.write(html)
    print(f"Rendered {out_file}")
