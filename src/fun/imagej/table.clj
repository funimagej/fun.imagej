(ns fun.imagej.table)

(defn create-column
  "Create a column for a table"
  [cname]
  ^net.imagej.table.GenericColumn (net.imagej.table.GenericColumn. cname))

(defn add-to-column
  "Add a value to a column. If idx is not specified, this appends"
  ([col val]
   (.add col val))
  ([col val idx]
   (.add col idx val)))

(defn create-table
  "Create a new table"
  []
  ^net.imagej.table.DefaultGenericTable (net.imagej.table.DefaultGenericTable.))

(defn add-column-to-table
  "Add a column to a table."
  [tbl col]
  (.add tbl col))
