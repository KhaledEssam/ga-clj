(ns ga-clj.core-test
  (:require [clojure.test :as t]
            [ga-clj.core :refer []]))

(t/deftest a-test
  (t/testing "0 == 0"
    (t/is (= 0 0))))
