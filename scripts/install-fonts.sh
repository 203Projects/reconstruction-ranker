#!/usr/bin/env bash
# Downloads Noto Sans KR + Noto Serif KR (Google Fonts) into composeApp resources.
# Bundled under SIL Open Font License 1.1 — see composeApp/src/commonMain/composeResources/font/OFL.txt
set -euo pipefail

DEST="$(cd "$(dirname "$0")/.."; pwd)/composeApp/src/commonMain/composeResources/font"
mkdir -p "$DEST"

declare -A FONTS=(
  ["NotoSansKR-Regular.ttf"]="https://fonts.gstatic.com/s/notosanskr/v39/PbyxFmXiEBPT4ITbgNA5Cgms3VYcOA-vvnIzzuoyeLQ.ttf"
  ["NotoSansKR-Medium.ttf"]="https://fonts.gstatic.com/s/notosanskr/v39/PbyxFmXiEBPT4ITbgNA5Cgms3VYcOA-vvnIzztgyeLQ.ttf"
  ["NotoSansKR-SemiBold.ttf"]="https://fonts.gstatic.com/s/notosanskr/v39/PbyxFmXiEBPT4ITbgNA5Cgms3VYcOA-vvnIzzjQ1eLQ.ttf"
  ["NotoSansKR-Bold.ttf"]="https://fonts.gstatic.com/s/notosanskr/v39/PbyxFmXiEBPT4ITbgNA5Cgms3VYcOA-vvnIzzg01eLQ.ttf"
  ["NotoSerifKR-Medium.ttf"]="https://fonts.gstatic.com/s/notoserifkr/v31/3JnoSDn90Gmq2mr3blnHaTZXbOtLJDvui3JOncjUeM52.ttf"
  ["NotoSerifKR-SemiBold.ttf"]="https://fonts.gstatic.com/s/notoserifkr/v31/3JnoSDn90Gmq2mr3blnHaTZXbOtLJDvui3JOncg4f852.ttf"
  ["NotoSerifKR-Bold.ttf"]="https://fonts.gstatic.com/s/notoserifkr/v31/3JnoSDn90Gmq2mr3blnHaTZXbOtLJDvui3JOncgBf852.ttf"
)

for name in "${!FONTS[@]}"; do
  target="$DEST/$name"
  if [[ -f "$target" ]] && [[ $(stat -f%z "$target" 2>/dev/null || stat -c%s "$target") -gt 1000000 ]]; then
    echo "  [skip] $name (already present)"
    continue
  fi
  echo "  [fetch] $name"
  curl -sSL -o "$target" "${FONTS[$name]}"
done

cat > "$DEST/OFL.txt" <<'EOF'
Copyright 2017 The Noto Project Authors (https://github.com/notofonts/noto-cjk)

This Font Software is licensed under the SIL Open Font License, Version 1.1.
Full license: https://openfontlicense.org/
EOF

echo "Fonts installed at: $DEST"
