"""Draws the BlastPotion banner, 1200x400: the icon on the left, the name and the tagline on dark stone."""
from icon import PIXELS, pixel_art
from textures import texture

WIDTH, HEIGHT = 1200, 400
# One pixel of the icon's art in pixels of the banner, so that every edge lands on a whole pixel of the PNG
ICON_SCALE = 10
RED, RED_SHADOW = '#ff5555', '#3f1515'
LIGHT, LIGHT_SHADOW = '#e6e6e6', '#393939'
TAGLINE = ('Craftable splash potions', 'that explode where they land')


def hex_color(rgba):
    r, g, b, _ = rgba
    return '#%02x%02x%02x' % (r, g, b)


def pattern(pattern_id, tex, size):
    """A texture repeated over the whole area, every texel a square."""
    cell = size / len(tex)
    rects = ''.join('<rect x="%.2f" y="%.2f" width="%.2f" height="%.2f" fill="%s"/>' % (
        u * cell, v * cell, cell + 0.3, cell + 0.3, hex_color(rgba)) for v, row in enumerate(tex)
                    for u, rgba in enumerate(row))
    return '<pattern id="%s" patternUnits="userSpaceOnUse" width="%d" height="%d">%s</pattern>' % (
        pattern_id, size, size, rects)


class Font:
    """The Minecraft font from the client jar: a 16x16 grid of 8x8 cells."""

    def __init__(self):
        self.pixels = texture('font/ascii')

    def glyph(self, char):
        """The pixels of a character and how far the next one starts."""
        if char == ' ':
            return [], 4
        code = ord(char)
        cx, cy = (code % 16) * 8, (code // 16) * 8
        pixels = [(x, y) for y in range(8) for x in range(8) if self.pixels[cy + y][cx + x][3] > 0]
        return pixels, max(x for x, _ in pixels) + 2

    def width(self, content, scale):
        return (sum(self.glyph(char)[1] for char in content) - 1) * scale

    def text(self, content, x, y, scale, color):
        """The text as one path of squares."""
        rects = []
        cursor = x
        for char in content:
            pixels, advance = self.glyph(char)
            rects += ['M%d %dh%dv%dh-%dz' % (cursor + px * scale, y + py * scale, scale, scale, scale)
                      for px, py in pixels]
            cursor += advance * scale
        return '<path d="%s" fill="%s"/>' % (''.join(rects), color)

    def shadowed(self, content, x, y, scale, color, shadow):
        """Minecraft-style text with its shadow one pixel down and right."""
        return self.text(content, x + scale, y + scale, scale, shadow) + self.text(content, x, y, scale, color)


def banner():
    font = Font()
    icon_size = len(PIXELS) * ICON_SCALE
    icon_x, icon_y = 20, (HEIGHT - icon_size) // 2
    text_x, title_y, title_scale, line_scale = 410, 92, 11, 5
    widest = max([font.width('BlastPotion', title_scale)] + [font.width(line, line_scale) for line in TAGLINE])
    assert text_x + widest + title_scale <= WIDTH - 20, 'the text does not fit into the banner'

    defs = (pattern('stone', texture('block/deepslate'), 64)
            + '<linearGradient id="fade" x1="0" x2="1"><stop offset="0" stop-color="#0e0f12" stop-opacity="0.5"/>'
              '<stop offset="1" stop-color="#0e0f12" stop-opacity="0.92"/></linearGradient>'
              '<radialGradient id="blast" cx="0.18" cy="0.62" r="0.5">'
              '<stop offset="0" stop-color="#ff5a1f" stop-opacity="0.4"/>'
              '<stop offset="1" stop-color="#ff5a1f" stop-opacity="0"/></radialGradient>')
    body = ''.join('<rect width="%d" height="%d" fill="url(#%s)"/>' % (WIDTH, HEIGHT, fill)
                   for fill in ('stone', 'fade', 'blast'))
    body += pixel_art(icon_x, icon_y, ICON_SCALE)
    body += font.shadowed('BlastPotion', text_x, title_y, title_scale, RED, RED_SHADOW)
    line_y = title_y + 8 * title_scale + 42
    for line in TAGLINE:
        body += font.shadowed(line, text_x, line_y, line_scale, LIGHT, LIGHT_SHADOW)
        line_y += 10 * line_scale + 8
    return ('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 %d %d" width="%d" height="%d"><defs>%s</defs>%s'
            '</svg>\n' % (WIDTH, HEIGHT, WIDTH, HEIGHT, defs, body))
