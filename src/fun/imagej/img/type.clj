(ns fun.imagej.img.type)

(defn double-type
  "Return an ImgLib2 DoubleType."
  []
  (net.imglib2.type.numeric.real.DoubleType.))

(defn int-type
  "Return an ImgLib2 IntType."
  []
  (net.imglib2.type.numeric.integer.IntType.))

(defn long-type
  "Return an ImgLib2 LongType."
  []
  (net.imglib2.type.numeric.integer.LongType.))

(defn byte-type
  "Return an ImgLib2 ByteType."
  []
  (net.imglib2.type.numeric.integer.ByteType.))

(defn bit-type
  "Return an ImgLib2 BitType."
  []
  (net.imglib2.type.logic.BitType.))

(defn argb-type
  "Return an ImgLib2 ARGBType."
  []
  (net.imglib2.type.numeric.ARGBType.))

(defn argb-double-type
  "Return an ImgLib2 ARGBDoubleType."
  []
  (net.imglib2.type.numeric.ARGBDoubleType.))

(defn unsigned-byte-type
  "Return an ImgLib2 UnsignedByteType."
  []
  (net.imglib2.type.numeric.integer.UnsignedByteType.))

(defn unsigned-short-type
  "Return an ImgLib2 UnsignedByteType."
  []
  (net.imglib2.type.numeric.integer.UnsignedShortType.))

(defn unsigned-int-type
  "Retun an ImgLib2 UnsignedIntType."
  []
  (net.imglib2.type.numeric.integer.UnsignedIntType.))

(defn unsigned-long-type
  "Return an ImgLib2 UnsignedLongType."
  []
  (net.imglib2.type.numeric.integer.UnsignedLongType.))


(defmulti get-type-val class);"Return the value of an imglib2 type."
(defmethod get-type-val net.imglib2.type.numeric.real.FloatType [tpe] 
  ^float (.get ^net.imglib2.type.numeric.real.FloatType tpe))
(defmethod get-type-val net.imglib2.type.numeric.integer.UnsignedShortType [tpe] 
  (.get ^net.imglib2.type.numeric.integer.UnsignedShortType tpe))
(defmethod get-type-val net.imglib2.type.numeric.real.DoubleType [tpe] 
  ^double (.get ^net.imglib2.type.numeric.real.DoubleType tpe))
(defmethod get-type-val net.imglib2.type.numeric.integer.IntType [tpe] 
  ^int (.get ^net.imglib2.type.numeric.integer.IntType tpe))   
(defmethod get-type-val net.imglib2.type.numeric.integer.LongType [tpe] 
  ^long (.get ^net.imglib2.type.numeric.integer.LongType tpe))
(defmethod get-type-val net.imglib2.type.numeric.integer.ByteType [tpe] 
  (.get ^byte ^net.imglib2.type.numeric.integer.ByteType tpe))
(defmethod get-type-val net.imglib2.type.logic.BitType [tpe] 
  (.get ^net.imglib2.type.logic.BitType tpe))
(defmethod get-type-val net.imglib2.type.numeric.ARGBType [tpe] 
  (.get ^net.imglib2.type.numeric.ARGBType tpe))
(defmethod get-type-val net.imglib2.type.numeric.ARGBDoubleType [tpe] 
  (.get ^net.imglib2.type.numeric.ARGBDoubleType tpe))
(defmethod get-type-val net.imglib2.type.numeric.integer.UnsignedByteType [tpe] 
  (.get ^net.imglib2.type.numeric.integer.UnsignedByteType tpe))
(defmethod get-type-val net.imglib2.type.numeric.integer.UnsignedIntType [tpe] 
  (.get ^net.imglib2.type.numeric.integer.UnsignedIntType tpe))
(defmethod get-type-val net.imglib2.type.numeric.integer.UnsignedLongType [tpe] 
  (.get ^net.imglib2.type.numeric.integer.UnsignedLongType tpe))

(defmulti set-type-val (fn [tpe val] (class tpe)));"Return the value of an imglib2 type."
(defmethod set-type-val net.imglib2.type.numeric.real.FloatType [tpe val] 
  (.set ^net.imglib2.type.numeric.real.FloatType tpe ^float val))
(defmethod set-type-val net.imglib2.type.numeric.integer.UnsignedShortType [tpe val] 
  (.set ^net.imglib2.type.numeric.integer.UnsignedShortType tpe ^int val))
(defmethod set-type-val net.imglib2.type.numeric.real.DoubleType [tpe val] 
  (.set ^net.imglib2.type.numeric.real.DoubleType tpe ^double val))
(defmethod set-type-val net.imglib2.type.numeric.integer.IntType [tpe val] 
  (.set ^net.imglib2.type.numeric.integer.IntType tpe ^int val))
(defmethod set-type-val net.imglib2.type.numeric.integer.LongType [tpe val] 
  (.set ^net.imglib2.type.numeric.integer.LongType tpe ^long val))
(defmethod set-type-val net.imglib2.type.numeric.integer.ByteType [tpe val] 
  (.set ^net.imglib2.type.numeric.integer.ByteType tpe ^byte val))
(defmethod set-type-val net.imglib2.type.logic.BitType [tpe val] 
  (.set ^net.imglib2.type.logic.BitType tpe ^boolean val))
(defmethod set-type-val net.imglib2.type.numeric.ARGBType [tpe val] 
  (.set ^net.imglib2.type.numeric.ARGBType tpe ^int val))
#_(defmethod set-type-val net.imglib2.type.numeric.ARGBDoubleType [tpe a r g b] 
   (.set ^net.imglib2.type.numeric.ARGBDoubleType tpe ^double a ^double r ^double g ^double b))
(defmethod set-type-val net.imglib2.type.numeric.integer.UnsignedByteType [tpe val] 
  (.set ^net.imglib2.type.numeric.integer.UnsignedByteType tpe ^int val))
(defmethod set-type-val net.imglib2.type.numeric.integer.UnsignedIntType [tpe val] 
  (.set ^net.imglib2.type.numeric.integer.UnsignedIntType tpe ^long val))
(defmethod set-type-val net.imglib2.type.numeric.integer.UnsignedLongType [tpe val] 
  (.set ^net.imglib2.type.numeric.integer.UnsignedLongType tpe ^long val))

;; Mutable
(defmulti set-type-val! (fn [tpe val] (class tpe)));"Return the value of an imglib2 type."
(defmethod set-type-val! net.imglib2.type.numeric.real.FloatType [tpe val]
  (.set ^net.imglib2.type.numeric.real.FloatType tpe ^float val))
(defmethod set-type-val! net.imglib2.type.numeric.integer.UnsignedShortType [tpe val]
  (.set ^net.imglib2.type.numeric.integer.UnsignedShortType tpe ^int val))
(defmethod set-type-val! net.imglib2.type.numeric.real.DoubleType [tpe val]
  (.set ^net.imglib2.type.numeric.real.DoubleType tpe ^double val))
(defmethod set-type-val! net.imglib2.type.numeric.integer.IntType [tpe val]
  (.set ^net.imglib2.type.numeric.integer.IntType tpe ^int val))
(defmethod set-type-val! net.imglib2.type.numeric.integer.LongType [tpe val]
  (.set ^net.imglib2.type.numeric.integer.LongType tpe ^long val))
(defmethod set-type-val! net.imglib2.type.numeric.integer.ByteType [tpe val]
  (.set ^net.imglib2.type.numeric.integer.ByteType tpe ^byte val))
(defmethod set-type-val! net.imglib2.type.logic.BitType [tpe val]
  (.set ^net.imglib2.type.logic.BitType tpe ^boolean val))
(defmethod set-type-val! net.imglib2.type.numeric.ARGBType [tpe val]
  (.set ^net.imglib2.type.numeric.ARGBType tpe ^int val))
#_(defmethod set-type-val! net.imglib2.type.numeric.ARGBDoubleType [tpe a r g b]
    (.set ^net.imglib2.type.numeric.ARGBDoubleType tpe ^double a ^double r ^double g ^double b))
(defmethod set-type-val! net.imglib2.type.numeric.integer.UnsignedByteType [tpe val]
  (.set ^net.imglib2.type.numeric.integer.UnsignedByteType tpe ^int val))
(defmethod set-type-val! net.imglib2.type.numeric.integer.UnsignedIntType [tpe val]
  (.set ^net.imglib2.type.numeric.integer.UnsignedIntType tpe ^long val))
(defmethod set-type-val! net.imglib2.type.numeric.integer.UnsignedLongType [tpe val]
  (.set ^net.imglib2.type.numeric.integer.UnsignedLongType tpe ^long val))
