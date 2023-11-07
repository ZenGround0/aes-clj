(ns aes-timing-attack.aes
  (:require [buddy.core.codecs :as codecs]))

(defn bar
    "I don't do a whole lot."
  [x]
  (println x "Hello, Mars!"))

(defn bytes->hex [bs]
  (codecs/bytes->hex bs))

(defn hex->bytes [str]
  (codecs/hex->bytes str))

(defn unsigned [i]
  (if ( >= i 0)
    i
    (+ 128
       (- i -128))))

(defn xtimes [b]
  "GF multiplication by polynomial 'x' aka 2"
  (let [shifted (bit-shift-left (bit-and 0x7f b) 1)]
    (if (bit-test b 7)
      (bit-xor shifted 0x1b)
      shifted)))

(defn gmul3 [a]
  (bit-xor (xtimes a) a))

(defn gmul [a, b]
  "b = qn2^n + q(n-1)2^(n-1).. q0, qs are either 1 or 0
   a*b = (xor (qi2^i * a))
   multiplication by powers of 2 is done with xtimes
  "
  (reduce
   bit-xor 
   (map
    (fn [i]
      (if (bit-test b i)
        ;; + 2^i * a
        (nth (iterate xtimes a) i)
        0))
    (range 0 8))))

(defn Sbox []
  "Return array of bytes matching S box"
  (into-array
   (map
    #(first (codecs/hex->bytes %))
    [
     "63" "7c" "77" "7b" "f2" "6b" "6f" "c5" "30" "01" "67" "2b" "fe" "d7" "ab" "76"
     "ca" "82" "c9" "7d" "fa" "59" "47" "f0" "ad" "d4" "a2" "af" "9c" "a4" "72" "c0"
     "b7" "fd" "93" "26" "36" "3f" "f7" "cc" "34" "a5" "e5" "f1" "71" "d8" "31" "15"
     "04" "c7" "23" "c3" "18" "96" "05" "9a" "07" "12" "80" "e2" "eb" "27" "b2" "75"
     "09" "83" "2c" "1a" "1b" "6e" "5a" "a0" "52" "3b" "d6" "b3" "29" "e3" "2f" "84"
     "53" "d1" "00" "ed" "20" "fc" "b1" "5b" "6a" "cb" "be" "39" "4a" "4c" "58" "cf"
     "d0" "ef" "aa" "fb" "43" "4d" "33" "85" "45" "f9" "02" "7f" "50" "3c" "9f" "a8"
     "51" "a3" "40" "8f" "92" "9d" "38" "f5" "bc" "b6" "da" "21" "10" "ff" "f3" "d2"
     "cd" "0c" "13" "ec" "5f" "97" "44" "17" "c4" "a7" "7e" "3d" "64" "5d" "19" "73"
     "60" "81" "4f" "dc" "22" "2a" "90" "88" "46" "ee" "b8" "14" "de" "5e" "0b" "db"
     "e0" "32" "3a" "0a" "49" "06" "24" "5c" "c2" "d3" "ac" "62" "91" "95" "e4" "79"
     "e7" "c8" "37" "6d" "8d" "d5" "4e" "a9" "6c" "56" "f4" "ea" "65" "7a" "ae" "08"
     "ba" "78" "25" "2e" "1c" "a6" "b4" "c6" "e8" "dd" "74" "1f" "4b" "bd" "8b" "8a"
     "70" "3e" "b5" "66" "48" "03" "f6" "0e" "61" "35" "57" "b9" "86" "c1" "1d" "9e"
     "e1" "f8" "98" "11" "69" "d9" "8e" "94" "9b" "1e" "87" "e9" "ce" "55" "28" "df"
     "8c" "a1" "89" "0d" "bf" "e6" "42" "68" "41" "99" "2d" "0f" "b0" "54" "bb" "16"
     ])))

(defn SboxInv []
  "Return an array of bytes matching inverse S box"
  (into-array
   (map
    #(first (codecs/hex->bytes %))
    [
     "52" "09" "6a" "d5" "30" "36" "a5" "38" "bf" "40" "a3" "9e" "81" "f3" "d7" "fb"
     "7c" "e3" "39" "82" "9b" "2f" "ff" "87" "34" "8e" "43" "44" "c4" "de" "e9" "cb"
     "54" "7b" "94" "32" "a6" "c2" "23" "3d" "ee" "4c" "95" "0b" "42" "fa" "c3" "4e"
     "08" "2e" "a1" "66" "28" "d9" "24" "b2" "76" "5b" "a2" "49" "6d" "8b" "d1" "25"
     "72" "f8" "f6" "64" "86" "68" "98" "16" "d4" "a4" "5c" "cc" "5d" "65" "b6" "92"
     "6c" "70" "48" "50" "fd" "ed" "b9" "da" "5e" "15" "46" "57" "a7" "8d" "9d" "84"
     "90" "d8" "ab" "00" "8c" "bc" "d3" "0a" "f7" "e4" "58" "05" "b8" "b3" "45" "06"
     "d0" "2c" "1e" "8f" "ca" "3f" "0f" "02" "c1" "af" "bd" "03" "01" "13" "8a" "6b"
     "3a" "91" "11" "41" "4f" "67" "dc" "ea" "97" "f2" "cf" "ce" "f0" "b4" "e6" "73"
     "96" "ac" "74" "22" "e7" "ad" "35" "85" "e2" "f9" "37" "e8" "1c" "75" "df" "6e"
     "47" "f1" "1a" "71" "1d" "29" "c5" "89" "6f" "b7" "62" "0e" "aa" "18" "be" "1b"
     "fc" "56" "3e" "4b" "c6" "d2" "79" "20" "9a" "db" "c0" "fe" "78" "cd" "5a" "f4"
     "1f" "dd" "a8" "33" "88" "07" "c7" "31" "b1" "12" "10" "59" "27" "80" "ec" "5f"
     "60" "51" "7f" "a9" "19" "b5" "4a" "0d" "2d" "e5" "7a" "9f" "93" "c9" "9c" "ef"
     "a0" "e0" "3b" "4d" "ae" "2a" "f5" "b0" "c8" "eb" "bb" "3c" "83" "53" "99" "61"
     "17" "2b" "04" "7e" "ba" "77" "d6" "26" "e1" "69" "14" "63" "55" "21" "0c" "7d"
     ])))

(defn sub-word [word sbox]
  "Input a length four sequence of bytes (word) apply Sbox substitution to every byte
   Sbox is input for efficiency so that this operation won't regenerate each time.
   To apply the inverse function simply pass the sbox inverse."
  (map
   (fn[b]
     (get sbox (unsigned b)))
   word))

(defn round-const-schedule []
  "the aes key-schedule round constant for round i
   = 2^(i-1) in the Rijndael field"
  (cons nil ;; 1 indexed so add never accessed field to index 0
        (map
         #(list % 0 0 0)
         (take 10 (iterate xtimes 1)))))

(defn word-xor [wa wb]
  (map #(bit-xor (first %) (second %)) (map vector wa wb)))

(defn rotate 
  "Take a sequence and left rotates it n steps. If n is negative, 
  the sequence is rotated right. Executes in O(n) time." 
  [n seq] 
  (let [c (count seq)] 
    (take c (drop (mod n c) (cycle seq)))))
    

(defn get-round-keys [key-bytes sbox]
  (let [Nb 4
        Nk ( / (count key-bytes) Nb)
        _ (assert (not (nil? (some #{Nk} '(4 6 8) )))) ;; assert AES 128, 196 or 256
        Nr ({4 10, 6 12, 8 15} Nk)
        rcon (round-const-schedule)
        init-words (apply vector (partition Nb Nb key-bytes))]

    (loop [i (count init-words), w init-words]
      (if (== i (* Nb (+ Nr 1)))
        w
        (let [last-word (last w)
              temp 
              (if (== 0 (mod i Nk))
                (word-xor
                 (sub-word (rotate 1 last-word) sbox)
                 (nth rcon (/ i Nk)))
                (if (and (> Nk 6) (== 4 (mod i Nk)))
                  (sub-word last-word sbox)
                  last-word))
              next-word (word-xor (nth w (- i Nk)) temp)]
          (recur (+ i 1) (conj w next-word)))))))

(defn state-xor [stA stB]
  (map #(word-xor (first %) (second %)) (map vector stA stB)))

(defn sub-state [state sbox]
  (map #(sub-word % sbox) state))

(defn transpose [m]
  (apply mapv vector m))

(defn shift-rows [state]
  "state is a vector of columns, rotate the rows"
  (let [rows (transpose state)]
    (transpose
     (map-indexed
      (fn [i word]
        (rotate i word))
      rows))))

(defn shift-rows-inv [state]
  "state is a vector of columns, rotate the rows in reverse"
  (let [rows (transpose state)]
    (transpose
     (map-indexed
      (fn [i word]
        (rotate (* -1 i) word))
      rows))))
  

(defn mix-columns [state]
  (letfn [(mix [i word]
            "rotate word by index and flatmap multiply by 0x02 0x03 0x01 0x01"
            (let [a [xtimes gmul3 identity identity]]
              (reduce bit-xor
                      (map-indexed (fn [idx itm]
                                     (apply (get a idx) [itm]))
                       (rotate i word)))))
          (mix-column [word]
            [ (mix 0 word)
              (mix 1 word)
              (mix 2 word)
              (mix 3 word)])
          ]
    (map mix-column state)))

(defn mix-columns-inv [state]
  (letfn [(mix [i word]
            "rotate word by index and flatmap multiply by 0x0e 0x0b 0x0d 0x09"
            (let [a [(partial gmul 0x0e) (partial gmul 0x0b)
                     (partial gmul 0x0d) (partial gmul 0x09)]]
                  (reduce bit-xor
                          (map-indexed (fn [idx itm]
                                         (apply (get a idx) [itm]))
                                       (rotate i word)))))
          (mix-column [word]
            [(mix 0 word)
             (mix 1 word)
             (mix 2 word)
             (mix 3 word)])
          ]
    (map mix-column state)))

(defn state-pretty [state]
  (reduce (fn [acc item] (str acc "|" item)) (map bytes->hex (map byte-array state))))

(defn state-string [state]
  (apply str (map bytes->hex (map byte-array state))))

(defn aes-enc [ptext-bytes, key-bytes]
  (let [sbox (Sbox)
        Nb 4
        round-keys (partition Nb (get-round-keys key-bytes sbox))
        Nk ( / (count key-bytes) Nb)
        _ (assert (not (nil? (some #{Nk} '(4 6 8) )))) ;; assert AES 128, 192 or 256
        _ (assert (== 4 (/ (count ptext-bytes) Nb))) ;; block size 128 
        Nr ({4 10, 6 12, 8 14} Nk)
        init-state (apply vector (partition Nb ptext-bytes))
        last-round (fn [state]
                     (state-xor
                      (shift-rows
                       (sub-state state sbox))
                      (nth round-keys Nr)))]
    (last-round (loop [r 1, state (state-xor init-state (nth round-keys 0))]
                  (if (== r Nr)
                    state
                    (let [new-state (state-xor
                                     (mix-columns
                                      (shift-rows
                                       (sub-state state sbox)))
                                     (nth round-keys r))]
                      (recur (+ r 1) new-state)))))))



(defn aes-dec [ctext-bytes, key-bytes]
  (let [sbox (Sbox)
        sbox-inv (SboxInv)
        Nb 4         
        round-keys (partition Nb (get-round-keys key-bytes sbox))
        Nk ( / (count key-bytes) Nb)
        _ (assert (not (nil? (some #{Nk} '(4 6 8) )))) ;; assert AES 128, 192 or 256
        _ (assert (== 4 (/ (count ctext-bytes) Nb))) ;; block size 128 
        Nr ({4 10, 6 12, 8 14} Nk)
        init-state (apply vector (partition Nb Nb ctext-bytes))
        last-round (fn [state]
                     (state-xor
                      (nth round-keys 0)
                      (sub-state 
                       (shift-rows-inv state)
                       sbox-inv)))]
    (last-round (loop [r (- Nr 1), state (state-xor init-state (nth round-keys Nr))]
                  (if (== r 0)
                    state
                    (let [new-state
                          (mix-columns-inv
                           (state-xor
                            (nth round-keys r)
                            (sub-state 
                             (shift-rows-inv state)
                             sbox-inv)))]
                      (recur (- r 1) new-state)))))))
