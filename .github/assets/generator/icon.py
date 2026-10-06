"""Draws the BlastPotion icon - a flask with TNT inside - as a pure vector SVG from its pixel art."""

SIZE = 512
# One pixel of the art in pixels of the icon, so that every edge lands on a whole pixel of the PNG
SCALE = 13

# The hand-made pixel art, 39x39; every character is a colour from PALETTE, a dot is transparent
PALETTE = {
    'a': '#d46d49', 'b': '#a94725', 'c': '#5d8fc2', 'd': '#973716', 'e': '#b3cfec', 'f': '#d4e5f7', 'g': '#db2908',
    'h': '#b00d23', 'i': '#eb4006', 'j': '#ea4007', 'k': '#ea4211', 'l': '#db2d14', 'm': '#b11427', 'n': '#b10d23',
    'o': '#902d11', 'p': '#db2f1a', 'q': '#b11527', 'r': '#ddd9d9', 's': '#ffffff', 't': '#cecfcf', 'u': '#cecece',
    'v': '#dedada', 'w': '#dddada', 'x': '#dddad9', 'y': '#ded9d9', 'z': '#313052', 'A': '#363455', 'B': '#090736',
    'C': '#302f53', 'D': '#cfcfcf', 'E': '#090836', 'F': '#343354', 'G': '#8badd0', 'H': '#beb1b2', 'I': '#111038',
    'J': '#353455', 'K': '#313053', 'L': '#333254', 'M': '#dedad9', 'N': '#323254', 'O': '#151238', 'P': '#0c0b37',
    'Q': '#100e37', 'R': '#beb2b3', 'S': '#912800', 'T': '#b10e22', 'U': '#b11125', 'V': '#912b0c', 'W': '#912900',
    'X': '#dc2e16',
}
PIXELS = (
    '...........................aaaaaa......',
    '...........................aaaaaa......',
    '...........................aaaaaa......',
    '........................aaaaaabbb......',
    '........................aaaaaabbb......',
    '........................aaaaaabbb......',
    '.....................cccdddbbbbbb......',
    '.....................cccdddbbbbbb......',
    '.....................cccdddbbbbbb......',
    '..................eee......dddddd......',
    '..................eee......dddddd......',
    '..................eee......dddddd......',
    '...............eee......eeeddd.........',
    '...............eee......eeeddd.........',
    '...............eee......eeeddd.........',
    '............fff.........eee............',
    '............fff.........eee............',
    '............fff.........eee............',
    '.........fff...eee.........eee.........',
    '.........fff...eee.........eee.........',
    '.........fff...eee.........eee.........',
    '......fffghieeejklmjklmjklmjklccc......',
    '......fffnoleeelpqolpqolpqolpqccc......',
    '......fffnoleeelpqolpqolpqolpqccc......',
    '......fffnoleeelpqolpqolpqolpqccc......',
    '......fffnoleeelpqolpqolpqolpqccc......',
    '......fffrsseeetuvrrtuvwxrssuvccc......',
    '......eeeussyzABvCDuEvCFGGGsrrccc......',
    '......eeerrrHDIDuJKDLuvFGGGrHuccc......',
    '......eeeHrsuMLyrNvKJuuOGGGsuyccc......',
    '......eeeussruPuvKuDPuuQGGGsruccc......',
    '......eeeRRRRRRRRRRRRRRRGGGRRRccc......',
    '......eeeSSTUVVTUVVTUVVTGGGUTWccc......',
    '.........cccXqoppqoppGGGpqoccc.........',
    '.........cccXqoppqoppGGGpqoccc.........',
    '.........cccXqoppqoppGGGpqoccc.........',
    '............ccccccccccccccc............',
    '............ccccccccccccccc............',
    '............ccccccccccccccc............',
)


def pixel_art(x, y, scale):
    """The pixel art with its top left corner at (x, y): one path per colour, made of the horizontal runs of it."""
    runs = {}
    for row_y, row in enumerate(PIXELS):
        start = 0
        while start < len(row):
            end = start
            while end < len(row) and row[end] == row[start]:
                end += 1
            if row[start] != '.':
                runs.setdefault(row[start], []).append('M%d %dh%dv1h-%dz' % (start, row_y, end - start, end - start))
            start = end
    paths = ''.join('<path d="%s" fill="%s"/>' % (''.join(rects), PALETTE[key]) for key, rects in runs.items())
    # crispEdges keeps the pixels sharp and seamless at any size
    return '<g transform="translate(%d %d) scale(%d)" shape-rendering="crispEdges">%s</g>' % (x, y, scale, paths)


def icon():
    """The icon as an SVG, the pixel art in the middle."""
    offset = (SIZE - len(PIXELS) * SCALE) // 2
    return '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 %d %d" width="%d" height="%d">%s</svg>\n' % (
        SIZE, SIZE, SIZE, SIZE, pixel_art(offset, offset, SCALE))
