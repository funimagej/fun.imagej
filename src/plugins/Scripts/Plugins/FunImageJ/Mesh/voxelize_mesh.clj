; @Dataset(label="Filename of mesh",description="Filename should be stl") mesh-filename
; @Float(label="Width") width
; @Float(label="Height") height
; @Float(label="Depth") depth
; @OUTPUT ImagePlus voxelized-imp

(ns plugins.Scripts.Plugins.FunImageJ.plugins.Scripts.Plugins.FunImageJ.Mesh.voxelize-mesh
  (:require [fun.imagej.img :as img]
            [fun.imagej.core :as ij]
            [fun.imagej.ops :as ops]
            [fun.imagej.img.shape :as shape]
            [fun.imagej.conversion :as convert]
            [fun.imagej.segmentation.imp :as ij1seg]
            [fun.imagej.img.cursor :as cursor]
            [fun.imagej.imp.roi :as roi]
            [fun.imagej.img.type :as imtype]
            [fun.imagej.mesh :as msh]
            [fun.imagej.imp :as ij1]
            [clojure.string :as string]))

(refer 'user)

#_(do ; Bindings for testing
  (def mesh-filename "/Users/kharrington/git/brevis/resources/obj/sphere.stl")
  (def width 100)
  (def height 100)
  (def depth 100))

(def mesh (msh/convert-to-opsmesh (msh/read-stl mesh-filename)))

(def voxelized-imp (convert/img->imp (ops/run-op "voxelization" (object-array [mesh width height depth]))))

(intern 'user 'voxelized-imp voxelized-imp)
;(ij1/show-imp voxelized-imp)


