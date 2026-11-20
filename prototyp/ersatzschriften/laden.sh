#!/bin/sh
# Lädt die freien Ersatzschriften (ADR-017) mit ihren Lizenzen in diesen Ordner und prüft die
# Prüfsummen. Die Schriften werden nicht eingecheckt.
#
#   Liberation Sans 2.1.5 (SIL OFL 1.1): Ersatz für Arial, gleiche Laufweiten
#   PT Sans Narrow, Encode Sans Condensed (SIL OFL 1.1): Kandidaten für Arial Narrow
#
# Aufruf im Ordner prototyp/:  sh ersatzschriften/laden.sh
set -eu
cd "$(dirname "$0")"
GF=https://raw.githubusercontent.com/google/fonts/f391eb5d1f3c7d64eab1bf992b1aee045ac48ffb/ofl
LIB=https://github.com/liberationfonts/liberation-fonts/files/7261482/liberation-fonts-ttf-2.1.5.tar.gz

lade() { # url datei sha256
  curl -sfL -o "$2" "$1"
  echo "$3  $2" | shasum -a 256 -c - >/dev/null || { echo "Prüfsumme falsch: $2" >&2; rm -f "$2"; exit 1; }
  echo "geladen: $2"
}

lade "$LIB" liberation.tar.gz 7191c669bf38899f73a2094ed00f7b800553364f90e2637010a69c0e268f25d0
tar -xzf liberation.tar.gz --strip-components=1 \
  liberation-fonts-ttf-2.1.5/LiberationSans-Regular.ttf liberation-fonts-ttf-2.1.5/LiberationSans-Bold.ttf \
  liberation-fonts-ttf-2.1.5/LiberationSans-Italic.ttf liberation-fonts-ttf-2.1.5/LiberationSans-BoldItalic.ttf \
  liberation-fonts-ttf-2.1.5/LICENSE
mv LICENSE LiberationSans-OFL.txt
rm liberation.tar.gz

lade "$GF/ptsansnarrow/PT_Sans-Narrow-Web-Regular.ttf" PT_Sans-Narrow-Web-Regular.ttf 4102edda03059163771869d258df54ac8563c408fa6e9ef75b2ddc85eabea6f4
lade "$GF/ptsansnarrow/PT_Sans-Narrow-Web-Bold.ttf" PT_Sans-Narrow-Web-Bold.ttf e69d83bcf5bd647892b4e2b22f5098dabd55c989413513197722fc156fb9f00e
lade "$GF/ptsansnarrow/OFL.txt" PTSansNarrow-OFL.txt 2758cf7a872827f39661cf8cc24188113c030447aefb5ca7145993650076ca8c
lade "$GF/encodesanscondensed/EncodeSansCondensed-Regular.ttf" EncodeSansCondensed-Regular.ttf 768d969127953c8a2e7d756a7735c7f803e08a3f86be60dcad77bafff3711788
lade "$GF/encodesanscondensed/EncodeSansCondensed-Bold.ttf" EncodeSansCondensed-Bold.ttf a6896b28cc8e16fe8d1073c766127a07a9c89cc8f4df9ffac376a41ff4050b5f
lade "$GF/encodesanscondensed/OFL.txt" EncodeSansCondensed-OFL.txt cc8c0c3e81cf543e190f128b83ba7c30959f15865c14dd972fca4917faeceec1
