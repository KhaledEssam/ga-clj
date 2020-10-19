(ns ga-clj.core-test
  (:require [clojure.test :as t]
            [ga-clj.genetic :as g]))

(t/deftest swap-test
  (t/testing "swap"
    (t/is (= (g/swap [1 2 3 4] 0 1) [2 1 3 4]))))
