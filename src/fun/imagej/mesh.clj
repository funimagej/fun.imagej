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

(defn merge-vertices-by-distance
  "Merge vertices that are close within a given distance threshold.
Expects vertices to be a sequence of RealLocalizable's.
Returns a sequence of RealLocalizable's"
  [vertices distance-threshold]
  (let [point-list ^net.imglib2.RealPointSampleList (net.imglib2.RealPointSampleList. 3)
        _ (doseq [vert vertices]
            (.add point-list ^net.imglib2.RealLocalizable vert;; Let's update this to use real localizables
              (net.imglib2.type.logic.BitType. false)))
        kdtree (net.imglib2.KDTree. point-list)
        tree-search ^net.imglib2.neighborsearch.RadiusNeighborSearchOnKDTree (net.imglib2.neighborsearch.RadiusNeighborSearchOnKDTree. kdtree)]
    ;; First search and remove
    (loop [cur ^net.imglib2.KDTree$KDTreeCursor (.cursor kdtree)]; mighe be $KDTreeCursor
      (when (.hasNext cur)
        (.fwd cur)        
        (when-not (.get ^net.imglib2.type.logic.BitType (.get cur)) ;; Only look at neighbors if this point is unflagged
          (.search tree-search cur distance-threshold true)        
          (when (pos? (.numNeighbors tree-search))          
            (doseq [k (range 1 (.numNeighbors tree-search))]; Unsure whether input point is returned, assuming so
              (.setOne ^net.imglib2.type.logic.BitType (.get ^net.imglib2.Sampler (.getSampler tree-search k))))))
        (recur cur)))
    ;; Then return unflagged points
    (loop [cur ^net.imglib2.KDTree$KDTreeCursor (.cursor kdtree)
           result-verts []]
      (if (.hasNext cur)
        (do 
          (.fwd cur)
          (if-not (.get ^net.imglib2.type.logic.BitType (.get cur)) ;; Only look at neighbors if this point is unflagged
            (recur cur (conj result-verts (net.imglib2.RealPoint. cur)))
            (recur cur result-verts)))
        result-verts))))
