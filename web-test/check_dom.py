import subprocess
import json

# Run headless chrome to dump layout metrics of the tables
script = """
(() => {
  const tables = document.querySelectorAll('table');
  const results = [];
  tables.forEach((t, i) => {
    const parent = t.parentElement;
    results.push({
      index: i,
      wrapped: parent.classList.contains('viewmd-table-wrap'),
      parentOverflowX: window.getComputedStyle(parent).overflowX,
      tableWidth: t.offsetWidth,
      parentWidth: parent.offsetWidth,
      scrollable: parent.scrollWidth > parent.clientWidth,
      firstColMinWidth: window.getComputedStyle(t.querySelector('th') || t.querySelector('td')).minWidth
    });
  });
  const bodyWidth = document.body.offsetWidth;
  const docScrollWidth = document.documentElement.scrollWidth;
  const docClientWidth = document.documentElement.clientWidth;
  return {
    tables: results,
    pageSidewaysScroll: docScrollWidth > docClientWidth,
    docScrollWidth,
    docClientWidth
  };
})()
"""

# Test with 400px width
cmd = [
    "google-chrome",
    "--headless=new",
    "--disable-gpu",
    "--window-size=400,1200",
    "--run-all-compositor-stages-before-draw",
    "web-test/fixtures/torture-test-light.html"
]
# We can evaluate via a small node runner with playwright or chrome-remote-interface
