(ns ga-clj.genetic-test
  (:require [clojure.test :as t]
            [ga-clj.genetic :as g]))

(t/deftest swap-test
  (t/testing "swap"
    (t/is (= (g/swap [1 2 3 4] 0 1) [2 1 3 4]))))

(t/deftest find-first-test
  (t/testing "find-first"
    (t/is (= 6 (g/find-first #(> % 5) (range 10))))))
