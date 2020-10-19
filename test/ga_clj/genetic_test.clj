(ns ga-clj.genetic-test
  (:require [clojure.test :as t]
            [ga-clj.genetic :as g]))

(t/deftest swap-test
  (t/testing "swap"
    (t/is (= [2 1 3 4] (g/swap [1 2 3 4] 0 1)))))

(t/deftest find-first-test
  (t/testing "find-first"
    (t/is (= 6 (g/find-first #(> % 5) (range 10))))))

(t/deftest cumsum-test
  (t/testing "cumsum"
    (t/is (= [1 2 3 4] (g/cumsum [1 1 1 1])))))

(t/deftest sum-test
  (t/testing "sum"
    (t/is (= 10 (g/sum [1 2 3 4])))))

(t/deftest div-test
  (t/testing "div"
    (t/is (= [1 1/2 2 3/2] (g/div [2 1 4 3] 2)))))
