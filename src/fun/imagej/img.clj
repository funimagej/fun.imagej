(ns fun.imagej.img
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [fun.imagej.img.cursor :as cursor]
            [fun.imagej.img.type :as imtype]
            [fun.imagej.ops :as ops])
  (:import [net.imglib2.algorithm.neighborhood Neighborhood RectangleShape]
           [net.imglib2.util Intervals]
           [net.imglib2.img ImagePlusAdapter Img]
           [net.imglib2.img.display.imagej ImageJFunctions]
           [net.imglib2.type NativeType]
           [net.imglib2.type.numeric NumericType ARGBType]
           [net.imglib2.type.numeric.real FloatType]
           [net.imglib2.algorithm.gauss3 Gauss3]
           [net.imglib2.algorithm.dog DifferenceOfGaussian]
           [net.imglib2.view Views IntervalView]
           [net.imglib2 Cursor RandomAccess RandomAccessibleInterval Interval IterableInterval]
           [net.imglib2.converter Converters]
           [net.imglib2.algorithm.binary Thresholder]
           [net.imglib2.algorithm.gradient PartialDerivative HessianMatrix]
           [net.imglib2.algorithm.linalg.eigen TensorEigenValues]
           ))

(defn num-dimensions
  "Return the number of dimensions of a EuclideanSpace."
  [^net.imglib2.EuclideanSpace input]
  (.numDimensions input))

(defn dimensions
  "Return the number of pixels in each dimension for something
with Dimensions."
  [^net.imglib2.Dimensions input]
  (let [dims ^longs (long-array (num-dimensions input))]
    (.dimensions input dims)
    dims))

(defn show
  "Display an Img."
  [^net.imglib2.RandomAccessibleInterval img]
  (net.imglib2.img.display.imagej.ImageJFunctions/show img)
  img)

(defn copy
  "Create a copy of an img."
  [^Img img]
  ^Img (.copy img))

(defn create-img-like
  "Create an Img like the input."
  ([^Img img tpe]
    ^Img (.create (.factory img)
           img
           tpe))
  ([^Img img]
    (create-img-like img (.firstElement img))))

(defn get-size-dimension
  "Return the size along the specified dimension."
  [^net.imglib2.Dimensions img d]
  (.dimension img d))

(defn get-width
  "Return the width of the img."
  [^net.imglib2.Dimensions img]
  (.dimension img 0))

(defn get-height
  "Return the height of the img."
  [^net.imglib2.Dimensions img]
  (.dimension img 1))

(defn get-depth
  "Return the depth of the image."
  [^net.imglib2.Dimensions img]
  (.dimension img 2))

(defn get-type
  "Return the class type of an image."
  [^net.imglib2.RandomAccessibleInterval img]
  (net.imglib2.util.Util/getTypeFromInterval img))

(defn get-val
  "Return the value at a given position."
  [^net.imglib2.RandomAccessibleInterval img ^longs position]
  (let [^RandomAccess ra (.randomAccess img)]
    (.setPosition ra position)
    (.get (.get ra))))

(defn set-val
  "Set the value at a given position."
  [^net.imglib2.RandomAccessibleInterval img ^longs position new-val]
  (let [^RandomAccess ra (.randomAccess img)]
    (.setPosition ra position)
    (.set (.get ra) new-val))
  img)

(defn map-img
   "Walk all images (as cursors) applying f at each step.
f is a function that operates on cursors in the same order as imgs
If you have an ImagePlus, then use funimage.conversion
Note: this uses threads to avoid some blocking issues."
   ([f img1]
     (let [cur1 ^Cursor (.cursor ^IterableInterval img1)
           t (Thread.
               (fn []
                 (loop []
                   (when (.hasNext cur1)
                     (.fwd cur1)
                     (f cur1)
                     (recur)))))]
       (.start t)
       (.join t)
       [img1]))
   ([f img1 img2]
     (let [^Cursor cur1 (.cursor ^IterableInterval img1)
           ^Cursor cur2 (.cursor ^IterableInterval img2)
           t (Thread.
               (fn []
                 (loop []
                   (when (and (.hasNext cur1)
                              (.hasNext cur2))
                     (.fwd cur1)
                     (.fwd cur2)
                     (f cur1 cur2)
                     (recur)))))]
       (.start t)
       (.join t)
       [img1 img2]))
   ([f img1 img2 & imgs]
     (let [imgs (concat [img1 img2] imgs)
           curs (map (fn ^Cursor [i] (.cursor ^IterableInterval i)) imgs)
           t (Thread.
               (fn []
                 (loop []
                   (when (every? #(.hasNext %) curs)
                     (doseq [cur curs] (.fwd cur))
                     (apply f curs)
                     (recur)))))]
       (.start t)
       (.join t)
       imgs)))
(def map-img! map-img)

(defn map-localize-img
   "Walk all images (as cursors) applying f at each step.
f is a function that operates on cursors in the same order as imgs
If you have an ImagePlus, then use funimage.conversion
Note: this uses threads to avoid some blocking issues."
   ([f img1]
     (let [^Cursor cur1 (.localizingCursor ^IterableInterval img1)
           t (Thread.
               (fn []
                 (loop []
                   (when (.hasNext cur1)
                     (.fwd cur1)
                     (f cur1)
                     (recur)))))]
       (.start t)
       (.join t)
       [img1]))
   ([f img1 img2]
     (let [^Cursor cur1 (.localizingCursor ^IterableInterval img1)
           ^Cursor cur2 (.localizingCursor ^IterableInterval img2)
           t (Thread.
               (fn []
                 (loop []
                   (when (and (.hasNext cur1)
                              (.hasNext cur2))
                     (.fwd cur1)
                     (.fwd cur2)
                     (f cur1 cur2)
                     (recur)))))]
       (.start t)
       (.join t)
       [img1 img2]))
   ([f img1 img2 & imgs]
     (let [imgs (concat [img1 img2] imgs)
           curs (map (fn ^Cursor [i] (.localizingCursor ^IterableInterval i)) imgs)
           t (Thread.
               (fn []
                 (loop []
                   (when (every? #(.hasNext %) curs)
                     (doseq [cur curs] (.fwd cur))
                     (apply f curs)
                     (recur)))))]
       (.start t)
       (.join t)
       imgs)))
(def map-localize-img! map-localize-img)

(defn resize
  "Resize an img."
  [img scale]
  (let [^net.imglib2.RandomAccessible extended-view (net.imglib2.view.Views/extendZero img)
        ^net.imglib2.RealRandomAccessible interpolated-view (net.imglib2.view.Views/interpolate extended-view (net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory.))
        ^net.imglib2.realtransform.AffineTransform2D transform (net.imglib2.realtransform.AffineTransform2D.)]
    (.scale transform scale)
    (net.imglib2.view.Views/interval
      (net.imglib2.view.Views/raster (net.imglib2.realtransform.RealViews/affineReal interpolated-view transform))
      (let [dims (dimensions img)]
        (net.imglib2.util.Intervals/createMinMax (long-array (concat (repeat (count dims) 0)
                                                                     (map #(* scale %) dims))))))))

; Nonthreaded version
#_(defn map-imgs
   "Walk all images (as cursors) applying f at each step.
f is a function that operates on cursors in the same order as imgs
If you have an ImagePlus, then use funimage.conversion"
   ([f img1]
     (let [cur1 (.cursor ^Img img1)]
       (loop []
         (when (.hasNext ^Cursor cur1)
           (.fwd ^Cursor cur1)
           (f cur1)
           (recur)))
       [img1]))
   ([f img1 img2]
     (let [cur1 (.cursor ^Img img1)
           cur2 (.cursor ^Img img2)]
       (loop []
         (when (and (.hasNext ^Cursor cur1)
                    (.hasNext ^Cursor cur2))
           (.fwd ^Cursor cur1)
           (.fwd ^Cursor cur2)
           (f cur1 cur2)
           (recur)))
       [img1 img2]))
   ([f img1 img2 & imgs]
     (let [imgs (concat [img1 img2] imgs)
           curs (map #(.cursor ^Img %) imgs)]
       (loop []
         (when (every? (map #(.hasNext ^Cursor %) curs))
           (doseq [cur curs] (.fwd ^Cursor cur))
           (apply f curs)
           (recur)))
       imgs)))

(defn replace
  "Replace img1 with img2"
  [^IterableInterval img1 ^IterableInterval img2]
  (second (map-img
            (fn [cur1 cur2] (.set (.get cur2) (.get cur1)))
            img2 img1)))
(def replace! replace)

(defn subtract
  "Subtract the second image from the first (destructive)."
  [^IterableInterval img1 ^IterableInterval img2]
  (first (map-img cursor/sub img1 img2)))
(def subtract! subtract)

(defn elmul
  "Subtract the second image from the first (destructive)."
  [^IterableInterval img1 ^IterableInterval img2]
  (first (map-img cursor/mul img1 img2)))
(def elmul! elmul)

(defn difference
  "Take the difference between two images."
  [^IterableInterval img1 ^IterableInterval img2]
  (first (map-img (fn [cur1 cur2]
                    (if (not= (cursor/get-val cur1) (cursor/get-val cur2)) 1 0))
                  img1 img2)))
(def difference! difference)

(defn filter-vals
  "Create a Bit Img that according to a function f, which should return true/false."
  [f ^IterableInterval img1]
  (let [bimg (create-img-like img1 (imtype/bit-type))]
    (second (map-img (fn [cur1 cur2]
                       (cursor/set-val cur2 (f (cursor/get-val cur1))))
                     img1 bimg))))
    
(defn scale
  "Scale the image."
  [^IterableInterval img scalar]
  (first (map-img #(cursor/set-val % 
                                   (* (cursor/get-val %) scalar)) img)))
(def scale! scale)

(defn threshold
  "Binarize an image about a threshold"
  [^Img img threshold]
  (Thresholder/threshold img
                         (let [tval (.copy (get-type img))]
                           (.set tval threshold)
                           tval)
                         true
                         1))
(def threshold! threshold)

(defn sum
  "Take the sum of all pixel values in an image."
  [^IterableInterval img]
  (let [sum (atom 0)]
    (map-img (fn [cur] (swap! sum + (cursor/get-val cur))) img)
    @sum))

#_(defn replace-subimg
    "Replace a subimage of a larger image with a smaller one."
    [img replacement start-x start-y]
    (let [offset (long-array [start-x start-y])
          rep-dim (image-dimensions replacement)
          stop-point (long-array (map #(dec (+ %1 %2)) offset rep-dim))
          subimg (Views/interval img offset stop-point)]
     (let [cur (.cursor ^Img replacement)
           ra (.randomAccess ^IntervalView subimg)
           pos (long-array 2)]
       (doseq [el rep-dim] (print el " ")) (println)
       (doseq [el (image-dimensions subimg)] (print el " ")) (println)
       (dotimes [k 2] (print (.min subimg k) " ")) (println )
       (dotimes [k 2] (print (.max subimg k) " ")) (println)
       (loop []
         (when (.hasNext ^Cursor cur)
           (.fwd ^Cursor cur)
           (.localize ^Cursor cur ^longs pos)
           (.setPosition ^RandomAccess ra
              ^longs pos)
           (.set (.get ra) (.get (.get cur)))
           (recur))))
     img))

(defn fill-boundary
  "Fill boundary pixels with the given value.
(bx,by,bz) - 'bottom' point. these are the small values. exclusive
(tx,ty,tz) - 'top' point. these are the big values. exclusive
locations outside these points are assigned fill-value"
  [^IterableInterval img bx by bz tx ty tz fill-value]; should take array of locations to generalize to N-D
  (let [location (float-array [0 0 0])
        f-fv (float fill-value)]
    (first (map-img (fn [cur]
                      (.localize cur location)
                      (when (or (< (first location) bx) (< (second location) by) (< (last location) bz)
                                (> (first location) tx) (> (second location) ty) (> (last location) tz))
                        (.set (.get cur)
                          f-fv)))
             img))))
(def fill-boundary! fill-boundary)

(defn neighborhood-map-to-center
  "Do a neighborhood walk over an imglib2 img.
Rectangle only"
  ([f radius source dest]
    (let [interval ^Interval (Intervals/expand source (* -1 radius))
          source ^RandomAccessibleInterval (Views/interval source interval)
          dest ^RandomAccessibleInterval (Views/interval dest interval)
          center ^Cursor (.cursor (Views/iterable dest))
          shape ^RectangleShape (RectangleShape. radius false)]
      (doseq [^Neighborhood local-neighborhood (.neighborhoods shape source)]
        (do (.fwd center)
          (f center local-neighborhood)))
      dest)))
(def neighborhood-map-to-center! neighborhood-map-to-center)

(defn periodic-neighborhood-map-to-center
  "Do a neighborhood walk over an imglib2 img.
Rectangle only"
  ([f radius source dest]
    (let [source (net.imglib2.view.Views/interval (net.imglib2.view.Views/extendPeriodic source) dest) ;^net.imglib2.view.ExtendedRandomAccessibleInterval
          center ^net.imglib2.Cursor (.cursor (net.imglib2.view.Views/iterable dest))
          shape ^net.imglib2.algorithm.neighborhood.RectangleShape (net.imglib2.algorithm.neighborhood.RectangleShape. radius false)
          local-neighborhood ^net.imglib2.algorithm.neighborhood.Neighborhood (.neighborhoods shape ^net.imglib2.RandomAccessibleInterval source)
          neighborhood-cursor ^net.imglib2.Cursor (.cursor local-neighborhood)]
      (loop []
        (when (.hasNext center)
          (.fwd center)
          (.fwd neighborhood-cursor)
          (f center ^net.imglib2.algorithm.neighborhood.RectangleNeighborhoodUnsafe (.get neighborhood-cursor))
          (recur)))
      dest)))
(def periodic-neighborhood-map-to-center! periodic-neighborhood-map-to-center)

(defn replace-subimg
  "Replace a subimage of a larger image with a smaller one."
  [img replacement start-position]
  (let [offset (long-array start-position)
        replacement-dim (long-array (count start-position))
        img-dim (long-array (count start-position))]
    (.dimensions replacement replacement-dim)
    (.dimensions img img-dim)
    (let [stop-point (long-array (map #(dec (+ %1 %2)) offset replacement-dim))
          subimg (Views/interval img offset stop-point)
          cur (.cursor ^IterableInterval replacement)
          ra (.randomAccess ^IntervalView subimg)
          pos (long-array (count start-position))]
      (map-img cursor/copy subimg replacement)))
    img)
(def replace-subimg! replace-subimg)

(defn replace-subimg-with-opacity
  "Replace a subimage of a larger image with a smaller one if the replacement is greater than the provided opacity value."
  [img replacement start-position opacity]
  (let [offset (long-array start-position)
        replacement-dim (long-array (count start-position))
        img-dim (long-array (count start-position))]
    (.dimensions replacement replacement-dim)
    (.dimensions img img-dim)
    (let [stop-point (long-array (map #(dec (+ %1 %2)) offset replacement-dim))
          subimg (Views/interval img offset stop-point)
          cur (.cursor ^IterableInterval replacement)
          ra (.randomAccess ^IntervalView subimg)
          pos (long-array (count start-position))]
      (map-img 
        (fn [cur1 cur2]
          (when (> (.get (.get cur2)) opacity)
            (.set ^net.imglib2.type.numeric.RealType (.get cur1) (.get cur2))))
        subimg replacement)))
    img)
(def replace-subimg-with-opacity! replace-subimg-with-opacity)

(defn confusion-img
  "Return an img that encodes the confusion matrix at each pixel."
  [^IterableInterval target ^IterableInterval pred]
  (last (map-img (fn [cur1 cur2 cur3]
                   (let [val1 (cursor/get-val cur1)
                         val2 (cursor/get-val cur2)]
                     (cursor/set-val cur3
                                     (if (or (pos? val1) (and (not (number? val1)) val1)); True target
                                       (if (or (pos? val2) (and (not (number? val2)) val2)); True prection
                                         1; t/t
                                         2);t/f
                                       (if (or (pos? val2) (and (not (number? val2)) val2)); True prection
                                         3; f/t
                                         4)))));f/f
                 target pred
                 (fun.imagej.ops.convert/uint8 (fun.imagej.ops.create/img target))
                 #_(create-img-like target (imtype/unsigned-byte-type)))))
  
(defn confusion-matrix
  "Return the confusion matrix from 2 images. The first image is taken to be the target and the second is the prediction."
  [^Img target ^Img pred]
  (let [confusion-img (confusion-img target pred)
        tt (sum (fun.imagej.ops.convert/uint8 (filter-vals #(= % 1) confusion-img)))
        tf (sum (fun.imagej.ops.convert/uint8 (filter-vals #(= % 2) confusion-img)))
        ft (sum (fun.imagej.ops.convert/uint8 (filter-vals #(= % 3) confusion-img)))
        ff (sum (fun.imagej.ops.convert/uint8 (filter-vals #(= % 4) confusion-img)))]
    {:TT tf
     :TF tf
     :FT ft
     :FF ff
     :F1 (/ (* tt 2) (+ tt tt ft ff))
     :ACC (/ (+ tt ff) (+ tt tf ft ff))}))

(defn gradient
  "Calculate the gradient with respect to a dimension using
the central difference method."
  [input dimension]
  (let [output (fun.imagej.ops.create/img input)]
    (PartialDerivative/gradientCentralDifference (Views/extendBorder input)
                                                 output dimension)
    output))

(defn concat-imgs
  "Concatenate images along dimension+1. All images
are assumed to be of the same size."
  [imgs]
  (let [dimensions (dimensions (first imgs))
        stack (fun.imagej.ops.create/img (long-array (concat dimensions
                                                             [(count imgs)])))]
    (dotimes [k (count imgs)]
      (map-img cursor/copy-real                   
                   (Views/hyperSlice stack (count dimensions) k)
                   (nth imgs k)))
    stack))

(defn hessian-matrix
  "Calculate the Hessian matrix, where gradients is a stack of images (presumably gradients)."
  [gradients]
  (let [hessians (fun.imagej.ops.create/img (long-array (concat (butlast (dimensions gradients))
                                                                [(* (dec (num-dimensions gradients))
                                                                    (num-dimensions gradients)
                                                                    (/ 2))])))]
    (HessianMatrix/calculateMatrix (Views/extendBorder gradients) hessians)
    hessians))

(defn tensor-eigen-values
  "Return the eigenvalues of rank 2 tensors."
  [^net.imglib2.RandomAccessibleInterval tensor]
  (let [eigenvals (fun.imagej.ops.create/img (long-array (concat (butlast (dimensions tensor))
                                                                 [(dec (last (dimensions tensor)))])))]
    (TensorEigenValues/calculateEigenValuesSymmetric tensor eigenvals)
    eigenvals))

(defn hyperslice
  "Return a n-1 dimensional slice through dimension d at position p."
  [^net.imglib2.RandomAccessibleInterval rai d pos]
  (Views/hyperSlice rai (int d) (long pos)))

(defn dimension-split
  "Split an image along the hyperslices of a dimension.
Returns a View"
  [^net.imglib2.RandomAccessibleInterval rai d]
  (map #(hyperslice rai d %) (range (get-size-dimension rai d))))

; Radius argument will be deprecated
(defn find-maxima
  "Find maxima"
  ([^net.imglib2.IterableInterval input]
   (let [radius 3]
    (find-maxima input ^net.imglib2.algorithm.neighborhood.RectangleShape (net.imglib2.algorithm.neighborhood.RectangleShape. radius true) radius)))
  ([^net.imglib2.IterableInterval input ^net.imglib2.algorithm.neighborhood.RectangleShape shp radius]
    (let [source (net.imglib2.view.Views/interval input (net.imglib2.util.Intervals/expand input (- radius)))
          nbrhoods (.neighborhoods shp source)]
      (loop [center-cur ^net.imglib2.Cursor (.cursor ^net.imglib2.IterableInterval (net.imglib2.view.Views/iterable source))
             nbr-cur ^net.imglib2.Cursor (.cursor nbrhoods)
             maxima []]
        (if-not (.hasNext center-cur)
          maxima
          (let [center (.next center-cur)]
            (.fwd nbr-cur)
            (if (reduce #(and %1 %2)
                        (map #(> (.compareTo ^net.imglib2.type.numeric.real.AbstractRealType center ^net.imglib2.type.numeric.real.AbstractRealType %) 0)
                             (iterator-seq (.iterator ^net.imglib2.algorithm.neighborhood.Neighborhood (.get nbr-cur)))))
              (let [pos (long-array (.numDimensions input))]
                (.localize center-cur pos)
                (recur center-cur nbr-cur (conj maxima (net.imglib2.Point. pos))))
              (recur center-cur nbr-cur maxima))))))))

(defn draw-maxima
  "Draw maxima into an image."
  [input maxima]
  (let [ra ^net.imglib2.RandomAccess (.randomAccess input)]
    (doseq [maximum maxima]
      (.setPosition ra ^net.imglib2.Localizable maximum)
      (when (net.imglib2.util.Intervals/contains input ra)
        (cursor/set-val ra 1))))
  input)

(defn realpoint-distance
  "Return the distance between two realpoints"
  [p1 p2]
  (let [a1 (double-array (.numDimensions p1))
        a2 (double-array (.numDimensions p2))
        diff (double-array (.numDimensions p1))]
    (.localize p1 a1)
    (.localize p2 a2)
    (net.imglib2.util.LinAlgHelpers/subtract a1 a2 diff)
    (net.imglib2.util.LinAlgHelpers/length diff)))

(defn point-distance
  "Return the distance between two Points"
  [p1 p2]
  (let [a1 (long-array (.numDimensions p1))
        a2 (long-array (.numDimensions p2))
        diff (long-array (.numDimensions p1))]
    (.localize p1 a1)
    (.localize p2 a2)
    (net.imglib2.util.LinAlgHelpers/subtract a1 a2 diff)
    (net.imglib2.util.LinAlgHelpers/length diff)))
