import os

os.makedirs("web-test/fixtures", exist_ok=True)

def generate_doc(size_kb, filename):
    target_bytes = size_kb * 1024
    content = [
        "---",
        f'summary: "Generated stress test fixture of size {size_kb} KB"',
        "triggers: [performance test, stress test]",
        "---",
        "",
        f"# Performance Stress Test: {size_kb} KB",
        "",
        "## Table of data",
        "",
        "| Row | Col 1 | Col 2 | Col 3 | Description | Long Token | Timestamp |",
        "|---|---|---|---|---|---|---|",
    ]
    
    current_len = sum(len(x) + 1 for x in content)
    row = 1
    while current_len < target_bytes:
        line = f"| {row} | alpha_{row} | beta_{row} | gamma_{row} | This is a benchmark stress test sentence verifying layout and typing latency. | https://example.com/item/{row}/data/token_hash_abcdef{row} | 2026-11-10T21:45:00Z |"
        content.append(line)
        current_len += len(line) + 1
        row += 1

    with open(filename, "w", encoding="utf-8") as f:
        f.write("\n".join(content))
    print(f"Generated {filename}: {os.path.getsize(filename)} bytes ({row} rows)")

if __name__ == "__main__":
    generate_doc(200, "web-test/fixtures/doc-200kb.md")
    generate_doc(1024, "web-test/fixtures/doc-1mb.md")
