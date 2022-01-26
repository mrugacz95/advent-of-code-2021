import json
from typing import Tuple, Dict

import open3d as o3d
from open3d.cpu.pybind.visualization import Visualizer, RenderOption

lines = 0
currentFrame = 0

with open("d22.json", 'r') as file:
    for line in file:
        lines += 1


class Cuboid:
    def __init__(self, x: Tuple[int, int], y: Tuple[int, int], z: Tuple[int, int]):
        self.z = z
        self.y = y
        self.x = x

    @staticmethod
    def fromDict(dict):
        x = (dict['x']['first'] - 1, dict['x']['last'])
        y = (dict['y']['first'] - 1, dict['y']['last'])
        z = (dict['z']['first'] - 1, dict['z']['last'])
        return Cuboid(x, y, z)

    @property
    def width(self):
        return self.x[1] - self.x[0]

    @property
    def height(self):
        return self.y[1] - self.y[0]

    @property
    def depth(self):
        return self.z[1] - self.z[0]

    def draw(self, state):
        mesh_box = o3d.geometry.TriangleMesh.create_box(width=self.width,
                                                        height=self.height,
                                                        depth=self.depth)
        mesh_box.translate((self.x[0], self.y[0], self.z[0]))
        color = [252 / 255.0, 196 / 255.0, 4 / 255.0] if state else [0.1, 0.1, 0.1]
        mesh_box.paint_uniform_color(color)
        # mesh_box.compute_vertex_normals()
        return mesh_box

    def drawWireFrame(self, color, volume):
        points = []
        for x in self.x:
            for y in self.y:
                for z in self.z:
                    points.append([x, y, z])
        lines = [
            [0, 1],
            [0, 2],
            [1, 3],
            [2, 3],
            [4, 5],
            [4, 6],
            [5, 7],
            [6, 7],
            [0, 4],
            [1, 5],
            [2, 6],
            [3, 7],
        ]
        line_set = o3d.geometry.LineSet(
            points=o3d.utility.Vector3dVector(points),
            lines=o3d.utility.Vector2iVector(lines),
        )
        colors = [color for i in range(len(lines))]
        line_set.colors = o3d.utility.Vector3dVector(colors)
        return line_set


class Plane:
    def __init__(self, x, y, z):
        self.z = z
        self.y = y
        self.x = x

    @staticmethod
    def fromDict(dict: Dict[str, int]):
        return Plane(dict.get('x', None), dict.get('y', None), dict.get('z', None))


def joinLists(l1, l2):
    if l2 is None and l1 is None:
        return None
    if l2 is None and l1 is not None:
        return l1
    if l1 is None and l2 is not None:
        return l2
    return l1 + l2


class BPSTree:
    def __init__(self, volume, cuboid, state, subtrees, splittingPlane):
        self.volume = volume
        self.cuboid = cuboid
        self.state = state
        self.subtrees = subtrees
        self.splittingPlane = splittingPlane

    @staticmethod
    def fromDict(dict):
        cuboid = Cuboid.fromDict(dict['cuboid']) if 'cuboid' in dict else None
        state = dict['state'] if 'state' in dict else None
        subtrees = [
            BPSTree.fromDict(dict['subtrees'][0]),
            BPSTree.fromDict(dict['subtrees'][1])
        ] if 'subtrees' in dict else None
        plane = Plane.fromDict(dict['splittingPlane']) if 'splittingPlane' in dict else None
        return BPSTree(Cuboid.fromDict(dict['volume']),
                       cuboid,
                       state,
                       subtrees,
                       plane
                       )

    def drawHypePlane(self):
        if self.splittingPlane is None:
            return None
        x = self.volume.x
        y = self.volume.y
        z = self.volume.z
        if self.splittingPlane.x is not None:
            x = [self.splittingPlane.x]
        if self.splittingPlane.y is not None:
            y = [self.splittingPlane.y]
        if self.splittingPlane.z is not None:
            z = [self.splittingPlane.z]
        points = []
        for xd in x:
            for yd in y:
                for zd in z:
                    points.append([xd, yd, zd])
        lines = [
            [0, 1],
            [1, 3],
            [3, 2],
            [2, 0],
        ]
        if len(points) != 4:
            raise RuntimeError("Bla")
        line_set = o3d.geometry.LineSet(
            points=o3d.utility.Vector3dVector(points),
            lines=o3d.utility.Vector2iVector(lines),
        )
        return line_set

    def draw(self, depth=0):
        # print(f"draw {depth}")
        lines_to_draw = None # self.volume.drawWireFrame([0, 0, 0], True)
        cubes_to_draw = None
        if self.cuboid is not None:
            if self.state:
                cubes_to_draw = joinLists(cubes_to_draw, self.cuboid.draw(self.state))
                lines_to_draw = joinLists(lines_to_draw, self.cuboid.drawWireFrame([0, 0, 0], True))
        # lines_to_draw = joinLists(lines_to_draw, self.drawHypePlane())
        if self.subtrees is not None:
            (ld, cd) = self.subtrees[0].draw(depth + 1)
            cubes_to_draw = joinLists(cubes_to_draw, cd)
            lines_to_draw = joinLists(lines_to_draw, ld)
            (ld, cd) = self.subtrees[1].draw(depth + 1)
            cubes_to_draw = joinLists(cubes_to_draw, cd)
            lines_to_draw = joinLists(lines_to_draw, ld)
        return lines_to_draw, cubes_to_draw


def parse(idx):
    with open("d22.json", 'r') as file:
        for i, l in enumerate(file):
            if i == idx:
                return BPSTree.fromDict(json.loads(l))


def custom_draw_geometry(lines, cubes):
    def rotate_view(vis):
        ctr = vis.get_view_control()
        ctr.rotate(2.0, 0.0)
        return False

    o3d.visualization.draw_geometries_with_animation_callback([lines] + [cubes],
                                                              rotate_view)
    # vis = o3d.visualization.Visualizer()
    # vis.create_window()
    # vis.get_render_option().light_on = False
    # vis.add_geometry(lines)
    # vis.add_geometry(cubes)
    # vis.run()
    # vis.destroy_window()


bpstree = parse(0)
print("parsed")
(lines3d, cubes3d) = bpstree.draw()
print("drawed")

# o3d.visualization.draw_geometries([lines3d] + [cubes3d])
custom_draw_geometry(lines3d, cubes3d)