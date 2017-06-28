(ns fun.imagej.mesh
  (:import [net.imagej.mesh.stl STLFacet BinarySTLFormat])
  (:require [fun.imagej.img :as img]
            [fun.imagej.imp :as imp]
            [fun.imagej.core :as ij]
            [fun.imagej.ops :as ops]
            [fun.imagej.conversion :as iconv]
            [clojure.java.io :as io]))

(defn vertex-to-vector3d
  "Convert a Vertex to Vector3D."
  [vtx]
  (org.apache.commons.math3.geometry.euclidean.threed.Vector3D.
    (.getX vtx)
    (.getY vtx)
    (.getZ vtx)))

(defn marching-cubes
  "Convenience function for marching cubes."
  [input]
  (fun.imagej.ops.geom/marchingCubes (fun.imagej.ops.convert/bit input)))

(defn write-mesh-as-stl
  "Write a DefaultMesh from imagej-ops to a .stl file."
  [mesh stl-filename]
  (let [stl-facets (for [facet (.getFacets mesh)]
                      (STLFacet. (vertex-to-vector3d (.getNormal facet)) 
                                 (vertex-to-vector3d (.getP0 facet))
                                 (vertex-to-vector3d (.getP1 facet))
                                 (vertex-to-vector3d (.getP2 facet))
                                 0))
        ofile (java.io.FileOutputStream. stl-filename)]
    (.write ofile
      (.write (BinarySTLFormat.)
        stl-facets))
    (.close ofile)))

(defn read-stl-mesh
  "Read a mesh."
  [stl-filename]
  (let [facets (.read (BinarySTLFormat.) (io/file stl-filename))]
    (for [facet facets
          vertex [(.vertex0 facet) (.vertex1 facet) (.vertex2 facet)]]
      vertex)))
