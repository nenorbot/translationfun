(ns translationfun.macros)

(defmacro with-alert [& form]
  `(try
     ~@form
     (catch js/Error e#
       (js/alert (str "Error:" e#)))))
