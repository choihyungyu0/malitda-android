# Figma 내보내기 SVG → Android VectorDrawable 변환기(단순 path/그라디언트 전용)
import re, sys, os, glob
import xml.etree.ElementTree as ET

SVGNS = "http://www.w3.org/2000/svg"


NAMED = {"white": "FFFFFF", "black": "000000", "red": "FF0000", "none": None}


def hexa(color, opacity=1.0):
    """#RGB/#RRGGBB/명명색 (+opacity) → #AARRGGBB"""
    c = color.strip().lower()
    if c in NAMED:
        if NAMED[c] is None:
            return "#00000000"
        c = NAMED[c]
    if c.startswith("#"):
        c = c[1:]
    if len(c) == 3:
        c = "".join(ch * 2 for ch in c)
    if len(c) == 6:
        a = round(max(0.0, min(1.0, opacity)) * 255)
        return "#%02X%s" % (a, c.upper())
    if len(c) == 8:  # 이미 AARRGGBB
        return "#" + c.upper()
    return "#FF000000"


def parse_grads(root):
    grads = {}
    for lg in root.iter("{%s}linearGradient" % SVGNS):
        gid = lg.get("id")
        stops = []
        for st in lg.findall("{%s}stop" % SVGNS):
            off = float(st.get("offset", "0"))
            col = st.get("stop-color", "#000000")
            op = float(st.get("stop-opacity", "1"))
            stops.append((off, col, op))
        grads[gid] = {
            "x1": lg.get("x1", "0"), "y1": lg.get("y1", "0"),
            "x2": lg.get("x2", "0"), "y2": lg.get("y2", "0"),
            "stops": stops,
        }
    return grads


def conv(path_in, path_out):
    tree = ET.parse(path_in)
    root = tree.getroot()
    vb = root.get("viewBox", "0 0 24 24").split()
    vw, vh = vb[2], vb[3]
    w = root.get("width", vw)
    h = root.get("height", vh)
    grads = parse_grads(root)

    out = []
    out.append('<vector xmlns:android="http://schemas.android.com/apk/res/android"')
    out.append('    xmlns:aapt="http://schemas.android.com/aapt"')
    out.append('    android:width="%sdp"' % w)
    out.append('    android:height="%sdp"' % h)
    out.append('    android:viewportWidth="%s"' % vw)
    out.append('    android:viewportHeight="%s">' % vh)

    npath = 0
    for p in root.iter("{%s}path" % SVGNS):
        d = p.get("d")
        if not d:
            continue
        npath += 1
        fill = p.get("fill")
        stroke = p.get("stroke")
        sw = p.get("stroke-width")
        cap = p.get("stroke-linecap")
        join = p.get("stroke-linejoin")
        frule = p.get("fill-rule")
        grad_id = None
        if fill and fill.startswith("url("):
            grad_id = re.search(r"#([^\)]+)", fill).group(1)

        attrs = ['android:pathData="%s"' % d.replace('"', "&quot;")]
        if fill and fill != "none" and not grad_id:
            attrs.append('android:fillColor="%s"' % hexa(fill))
        if stroke and stroke != "none":
            attrs.append('android:strokeColor="%s"' % hexa(stroke))
            if sw:
                attrs.append('android:strokeWidth="%s"' % sw)
            if cap:
                attrs.append('android:strokeLineCap="%s"' % cap)
            if join:
                attrs.append('android:strokeLineJoin="%s"' % join)
        if frule == "evenodd":
            attrs.append('android:fillType="evenOdd"')

        if grad_id and grad_id in grads:
            g = grads[grad_id]
            out.append("    <path")
            out.append("        " + "\n        ".join(attrs) + ">")
            out.append('        <aapt:attr name="android:fillColor">')
            out.append('            <gradient android:type="linear"')
            out.append('                android:startX="%s" android:startY="%s"' % (g["x1"], g["y1"]))
            out.append('                android:endX="%s" android:endY="%s">' % (g["x2"], g["y2"]))
            for (off, col, op) in g["stops"]:
                out.append('                <item android:offset="%s" android:color="%s"/>' % (off, hexa(col, op)))
            out.append("            </gradient>")
            out.append("        </aapt:attr>")
            out.append("    </path>")
        else:
            out.append("    <path")
            out.append("        " + "\n        ".join(attrs) + " />")

    out.append("</vector>")
    open(path_out, "w", encoding="utf-8").write("\n".join(out) + "\n")
    return npath


srcdir = r"C:\dev\malitda-android\design\devmaster\icons"
dstdir = r"C:\dev\malitda-android\app\src\main\res\drawable"
skip = {"s10_icons_layer", "ic_reward_heart"}  # 복합/빈 SVG는 별도 처리
done = []
for f in glob.glob(os.path.join(srcdir, "*.svg")):
    name = os.path.splitext(os.path.basename(f))[0]
    if name in skip or name.startswith("_"):
        continue
    try:
        n = conv(f, os.path.join(dstdir, name + ".xml"))
        done.append((name, n))
    except Exception as e:
        done.append((name, "ERR:%s" % e))
for name, n in sorted(done):
    print("%-18s paths=%s" % (name, n))
