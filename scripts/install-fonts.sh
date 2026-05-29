#!/usr/bin/env bash
# Downloads Noto Sans KR + Noto Serif KR (Google Fonts) into composeApp resources.
# Bundled under SIL Open Font License 1.1 — see composeApp/src/commonMain/composeResources/font/OFL.txt
#
# Works on macOS bash 3.2 and any modern bash/zsh.
set -eu

DEST="$(cd "$(dirname "$0")/.."; pwd)/composeApp/src/commonMain/composeResources/font"
mkdir -p "$DEST"

# name|url pairs — portable across bash 3.2 (no associative arrays)
FONTS="
NotoSansKR-Regular.ttf|https://fonts.gstatic.com/s/notosanskr/v39/PbyxFmXiEBPT4ITbgNA5Cgms3VYcOA-vvnIzzuoyeLQ.ttf
NotoSansKR-Medium.ttf|https://fonts.gstatic.com/s/notosanskr/v39/PbyxFmXiEBPT4ITbgNA5Cgms3VYcOA-vvnIzztgyeLQ.ttf
NotoSansKR-SemiBold.ttf|https://fonts.gstatic.com/s/notosanskr/v39/PbyxFmXiEBPT4ITbgNA5Cgms3VYcOA-vvnIzzjQ1eLQ.ttf
NotoSansKR-Bold.ttf|https://fonts.gstatic.com/s/notosanskr/v39/PbyxFmXiEBPT4ITbgNA5Cgms3VYcOA-vvnIzzg01eLQ.ttf
NotoSerifKR-Medium.ttf|https://fonts.gstatic.com/s/notoserifkr/v31/3JnoSDn90Gmq2mr3blnHaTZXbOtLJDvui3JOncjUeM52.ttf
NotoSerifKR-SemiBold.ttf|https://fonts.gstatic.com/s/notoserifkr/v31/3JnoSDn90Gmq2mr3blnHaTZXbOtLJDvui3JOncg4f852.ttf
NotoSerifKR-Bold.ttf|https://fonts.gstatic.com/s/notoserifkr/v31/3JnoSDn90Gmq2mr3blnHaTZXbOtLJDvui3JOncgBf852.ttf
"

# Portable file-size check (BSD stat on macOS, GNU stat on Linux)
filesize() {
  if stat -f%z "$1" >/dev/null 2>&1; then
    stat -f%z "$1"
  else
    stat -c%s "$1"
  fi
}

printf 'Installing fonts into %s\n' "$DEST"

echo "$FONTS" | while IFS='|' read -r name url; do
  [ -z "$name" ] && continue
  target="$DEST/$name"
  if [ -f "$target" ] && [ "$(filesize "$target")" -gt 1000000 ]; then
    printf '  [skip]  %s\n' "$name"
    continue
  fi
  printf '  [fetch] %s\n' "$name"
  if ! curl -sSL --fail -o "$target" "$url"; then
    printf '  [FAIL]  %s — see %s\n' "$name" "$url" >&2
    rm -f "$target"
    exit 1
  fi
done

cat > "$DEST/OFL.txt" <<'EOF'
Copyright 2017 The Noto Project Authors (https://github.com/notofonts/noto-cjk)

This Font Software is licensed under the SIL Open Font License, Version 1.1.
Full license: https://openfontlicense.org/
EOF

printf '\nDone. Fonts installed at: %s\n' "$DEST"
