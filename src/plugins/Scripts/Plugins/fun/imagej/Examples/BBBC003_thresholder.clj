(ns plugins.Scripts.Plugins.fun.imagej.Examples.BBBC003_thresholder)

; @ImagePlus(label="Input image",description="Input image") input-img
; @ImagePlus(label="Target image",description="Target image") target-img
; @OUTPUT ImagePlus thresholded

(require '[fun.imagej.img :as img]
         '[fun.imagej.core :as ij]
         '[fun.imagej.ops :as ops]
         '[fun.imagej.img.shape :as shape]
         '[fun.imagej.conversion :as convert]
         '[fun.imagej.segmentation.imp :as ij1seg]
         '[fun.imagej.imp.roi :as roi])

(def input-img (ij/open-img "/Users/kharrington/Data/BBBC/mouse_embryos_dic_images/7_19_M1E3.tif"))
(def target-img (ij/open-img "/Users/kharrington/Data/BBBC/mouse_embryos_dic_foreground/7_19_M1E3.png"))

(let [filtered (fun.imagej.ops.filter/variance (img/create-img-like input-img (net.imglib2.type.numeric.real.DoubleType.))
                                               input-img
                                               (shape/sphere-shape 15))
      thresholded (fun.imagej.ops.threshold/isoData filtered)
      rois (ij1seg/imp-to-rois (convert/img->imp thresholded))
      biggest-roi (convert/imp->img 
                    (roi/fill-rois (convert/img->imp (img/create-img-like thresholded))
                                   (take-last 1 (sort-by roi/area rois))
                                   (repeat 255)))]
  (println rois)
  (ij/show filtered "Filtered")
  (ij/show thresholded "Thresholded")
  (ij/show biggest-roi "Biggest roi"))

(ij/show input-img "Input")
(ij/show target-img "Target")
