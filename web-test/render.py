import re
from markdown_it import MarkdownIt

def render_fixture(source_md="docs/samples/torture-test.md", output_prefix="torture-test", mode="light"):
    with open(source_md, "r", encoding="utf-8") as f:
        text = f.read()

    # Frontmatter extraction
    fm_raw = ""
    m = re.match(r"^---[ \t]*\n(.*?)\n---[ \t]*\n", text, re.S)
    if m:
        fm_raw = m.group(1)
        text = text[m.end():]

    md = MarkdownIt("gfm-like")
    rendered_body = md.render(text)

    css = open("src/main/resources/web/viewmd.css", "r", encoding="utf-8").read()
    js = open("src/main/resources/web/postprocess.js", "r", encoding="utf-8").read()

    fm_html = f'<pre class="frontmatter"><code>---\n{fm_raw}\n---</code></pre>' if fm_raw else ""

    dark_override = "<style>:root { color-scheme: dark !important; --viewmd-fg:#e6edf3; --viewmd-bg:#0d1117; --viewmd-muted:#9198a1; --viewmd-line:#3d444d; --viewmd-soft:#151b23; --viewmd-code:#262c36; --viewmd-link:#4493f8; } html, body { background: #0d1117 !important; color: #e6edf3 !important; }</style>" if mode == "dark" else "<style>html, body { background: #ffffff; color: #1f2328; }</style>"

    title = "ViewMD Showcase" if "showcase" in output_prefix else "ViewMD Torture Test"
    html = f"""<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>{title} - {mode}</title>
  <style>{css}</style>
  {dark_override}
</head>
<body>
  {fm_html}
  {rendered_body}
  <script>{js}</script>
</body>
</html>"""

    filename = f"web-test/fixtures/{output_prefix}-{mode}.html"
    with open(filename, "w", encoding="utf-8") as f:
        f.write(html)
    print(f"Rendered to {filename}")

if __name__ == "__main__":
    render_fixture("docs/samples/torture-test.md", "torture-test", "light")
    render_fixture("docs/samples/torture-test.md", "torture-test", "dark")
    render_fixture("docs/samples/viewmd-showcase.md", "showcase", "light")
    render_fixture("docs/samples/viewmd-showcase.md", "showcase", "dark")
