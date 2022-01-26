from collections import defaultdict
from itertools import islice
from typing import Tuple, List

import matplotlib.pyplot as plt
from celluloid import Camera
from matplotlib.lines import Line2D

input_rules = """CH -> B
HH -> N
CB -> H
NH -> C
HB -> C
HC -> B
HN -> C
NN -> C
BH -> H
NC -> B
NB -> B
BN -> B
BB -> N
BC -> B
CC -> N
CN -> C"""


def window(seq, n=2):
    "Returns a sliding window (of width n) over data from the iterable"
    "   s -> (s0,s1,...s[n-1]), (s1,s2,...,sn), ...                   "
    it = iter(seq)
    result = tuple(islice(it, n))
    if len(result) == n:
        yield result
    for elem in it:
        result = result[1:] + (elem,)
        yield result


colors = [plt.cm.Pastel1(i) for i in range(20)]

MAX_Y = 30
MAX_X = 30
margin = 12


def main():
    initial = "NNCB"
    rules = {(f, s): v for (f, s), v in map(lambda i: i.split(" -> "), input_rules.split("\n"))}

    max_deep = 4

    counts = defaultdict(int)
    for p in rules.values():
        counts[p] = 0

    fig, ax = plt.subplots()
    plt.xticks([])
    camera = Camera(fig)

    def print_polymer(polymer, start_pos, end_pos):

        def calc_y(y):
            return MAX_Y - margin - y * 3.5

        def calc_x(x):
            return x * 2.5

        ax.set_ylim([-7, MAX_Y])
        types_to_plot = defaultdict(list)

        for x, (y, p) in enumerate(polymer):
            types_to_plot[p].append((y, x))
        last_point = None
        width = len(initial) + max_deep  # len(polymer)
        height = max_deep + 1

        for y in range(height):
            yline = MAX_Y - margin - y * 3.5
            plt.plot([0, width * 2.5], [yline, yline], color='lightgrey')

        arr = [[None] * width for _ in range(height)]
        for p, positions in types_to_plot.items():
            for (y, x) in positions:
                arr[y][x] = p
        for x in range(width):
            for y in range(height):
                p = arr[y][x]
                if p is not None:
                    draw_y, draw_x = calc_y(y), calc_x(x)
                    if last_point is not None:
                        if start_pos is not None and x - 1 in range(start_pos, end_pos):
                            line_color = 'salmon'
                        else:
                            line_color = 'black'
                        plt.plot([last_point[0], draw_x], [last_point[1], draw_y],
                                 color=line_color)
                    last_point = (draw_x, draw_y)
        for p, p_positions in types_to_plot.items():
            for y, x in list(map(lambda pos: (calc_y(pos[0]), calc_x(pos[1])), p_positions)):
                plt.plot(x, y,
                         color=colors[ord(p) % 7],
                         marker='o',
                         markersize=25,
                         label=p)
        for y, row in enumerate(arr):
            for x, p in enumerate(row):
                if p is not None:
                    print(p, end=' ')
                    draw_y = calc_y(y)
                    draw_x = calc_x(x)
                    ax.annotate(p, xy=(draw_x, draw_y), color='black',
                                fontsize="large", weight='heavy',
                                horizontalalignment='center',
                                verticalalignment='center')
                else:
                    print(end='  ')
            print()
        print()

        legend_elements = [Line2D([0], [0],
                                  color=colors[ord(p) % 7],
                                  marker='o',
                                  markersize=10,
                                  label=f'{p}: {count}') for p, count in counts.items()]
        ax.legend(handles=legend_elements)
        camera.snap()
        plt.yticks([calc_y(y) for y in range(height)],
                   range(height))
        ax.set_ylabel("Recursion depth")
        ax.spines["right"].set_visible(False)
        ax.spines["bottom"].set_visible(False)
        ax.spines["top"].set_visible(False)
        plt.title("[AOC 2021 Day 14] Recurrent solution")

    for p in initial:
        counts[p] += 1

    def count_polymer(depth, fragment: List[Tuple[int, str]], start_pos: int, end_pos: int):
        print_polymer(fragment, None, None)
        if depth == max_deep:
            return

        for pair_idx, pair in enumerate(window(fragment[start_pos:end_pos])):
            ((l_lvl, l), (r_lvl, r)) = pair
            if (l, r) in rules:
                insertion = rules[(l, r)]
                if insertion is not None:
                    counts[insertion] += 1
                    new_fragment = fragment[:max(start_pos + pair_idx, 0)] + \
                                   [(l_lvl, l), (depth + 1, insertion), (r_lvl, r)] + \
                                   fragment[start_pos + pair_idx + 2:]
                    print_polymer(fragment, start_pos + pair_idx, start_pos + pair_idx + 1)
                    count_polymer(depth + 1, new_fragment, start_pos + pair_idx, start_pos + pair_idx + 3)

        if depth == 0:
            for i in range(5):
                print_polymer(fragment, None, None)

    print(initial)
    print(rules)
    input_fragment = list(map(lambda x: (0, x), initial))
    count_polymer(0, input_fragment, 0, 4)
    print(counts)
    print(input_fragment)
    print(1588, max(counts.values()) - min(counts.values()))

    animation = camera.animate(interval=500, repeat_delay=1500, repeat=True)
    animation.save('day14.mp4', dpi=300)


if __name__ == '__main__':
    main()
