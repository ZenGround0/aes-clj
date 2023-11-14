import requests, random, time, json, re, scipy.stats
import matplotlib.pyplot as plt


def send_aes_plaintext(plaintext, url):
    headers = {
        "Content-Type": "text/plain"  # Assuming plaintext is just text
    }

    start = time.monotonic()
    response = requests.post(url, headers=headers, data=plaintext)
    end = time.monotonic()

    # Ensure the request was successful
    response.raise_for_status()

    # Return the output from the response body
    return response.text, end - start

def time_aes_plaintext(ptext, url="http://localhost:8080/hex"):
    resp, outer_time = send_aes_plaintext(ptext, url)
    return extract_time(resp), outer_time

def time_aes_get_ctext(ptext, url="http://localhost:8080/hex"):
    resp, outer_time = send_aes_plaintext(ptext, url)
    return extract_ctext(resp), extract_time(resp), outer_time

## return a random 16 byte plaintext
def random_plaintext():
    return random.getrandbits(128).to_bytes(16, 'little').hex()

## normalize times
def normalize_times(times, counts):
    normal = [[0 for x in range(256)] for y in range(16)]
    for i in range(16):
        for b in range(256):
            if counts[i][b] != 0:
                normal[i][b] = times[i][b] / counts[i][b]
    return normal
            

## tally result
def tally(ptext, encrypt_time, times, counts):
    for (i,b) in enumerate(bytes.fromhex(ptext)):
        try:
            t = times[i][b]
            cnt = counts[i][b]
        except KeyError:
            t = 0
            cnt = 0
        
        times[i][b] = t + encrypt_time
        counts[i][b] = cnt + 1

def extract_time(s):
    print(s)
    m = re.match('".*Elapsed time: (.*) cputicks"', s)
    return float(m.group(1))

def extract_ctext(s):
    m = re.match('"Ciphertext: (.*),.*"', s)
    return m.group(1)
        
def sweep_tally_time(n):
    times = [[0 for x in range(256)] for y in range(16)]
    counts = [[0 for x in range(256)] for y in range(16)]

    for i in range(n):
        if i % 1000 == 0:
            print(i)
        ptext = random_plaintext()
        inner_time, outer_time = time_aes_plaintext(ptext)
        if inner_time > 3000:
            print("time: {0}, i: {1}, ptext: {2}".format(inner_time, i, ptext))
        tally(ptext, inner_time, times, counts)

    return normalize_times(times, counts), counts

def save(timing, counts):
    records = []
    for i in range(16):
        for b in range(256):
            record = {'n': i, 'b': b.to_bytes(1, 'little').hex(), 't': timing[i][b], 'cnt': counts[i][b]}
            records.append(record)
    out = json.dumps(records)
    print(out)

def hit(n, ptext):
    times = []
    for i in range(n):
        if i % 1000 == 0:
            print(i)
        inner_time, _ = time_aes_plaintext(ptext)
        times.append(encrypt_time)
    
    print("avg encrypt time: {0}".format(sum(times) / len(times)))

def infinite_encrypt(ptext):
    while True:
        time_aes_plaintext(ptext)

# explore n random plaintext encryptions m times each
# print out summary statistics of drawn distributions
def explore(n, m):
    dists = {}
    ptexts = [ random_plaintext() for i in range(n) ]
    for i in range(m):
        if i % 100 == 0:        
            print(i)
        for ptext in ptexts:
            t, _ = time_aes_plaintext(ptext)
            if t > 3000:
                continue
            if not ptext in dists:
                dists[ptext] = []
            dists[ptext].append(t)
    tester = dists[ptexts[0]]
    for ptext in dists:
        data = dists[ptext]
        print("{0}: {1}, ks test: {2}".format(ptext, scipy.stats.describe(data), scipy.stats.ks_2samp(data, tester, alternative='two-sided', mode='auto').pvalue))


def sweep_score_ctext(n, lastkeyhex):
    '''
    Sweep random plaintexts, score output ciphertexts by count of Sbox duplicate hits.
    Sbox duplicate hits are measured by checking if ciphertext byte combinations include
    any lastkeyhex ciphertext byte combinations.  lastkeyhex is the last 4 words of the expanded
    round key bytes -- this function is validating against a known key.

    Sbox duplicates necessitate a cache hit when doing the final Sbox lookup in the T table
    implementation and therefore higher ciphertext score should roughly correlate to lower time.
    Noise comes from cache timing noise + the existence of cache hits unseen by duplicate
    Sbox hits -- ~8-16 Sbox entries are loaded in a cache line and will be hit without duplication
    '''
    lastkey_combinations = set(byte_combinations(lastkeyhex))
    scores = {}
    for i in range(n):
        ptext = random_plaintext()
        print(ptext)
        ctext, inner_time, outer_time = time_aes_get_ctext(ptext)
        # Filter outliers -- usually runtime artifacts
        if inner_time > 5000:
            print("time: {0}, i: {1}, ptext: {2}".format(inner_time, i, ptext))
            continue
        score = score_ctext(ctext, lastkey_combinations)
        if score not in scores:
            scores[score] = []
        scores[score].append(inner_time)
    tester = None
    for score in scores:
        data = scores[score]
        if tester is None:
            tester = data        
        print("score {0}: {1}, ks test: {2}".format(score, scipy.stats.describe(data), scipy.stats.ks_2samp(data, tester, alternative='two-sided', mode='auto').pvalue))
        
        
def score_ctext(ctexthex, match_set):
    score = 0
    ctext_combos = byte_combinations(ctexthex)
    for c in ctext_combos:
        if c in match_set:
            score += 1
    return score

def byte_combinations(hexstr):
    '''
    Return the set of the xor of each input byte with each other input byte
    excluding same bytes and repeats
    '''
    bs = enumerate(bytes.fromhex(hexstr))
    return [a ^ b for (i,a) in bs for (j, b) in bs if i < j]


        
def compare(m, ptext1, ptext2):
    dists = {}
    ptexts = [ptext1, ptext2]
    
    for i in range(m):
        if i % 100 == 0:        
            print(i)
        for ptext in ptexts:
            t, _ = time_aes_plaintext(ptext)
            # filter outliers, (for clojure likely JVM artifact)
            if t > 3000:
                continue
            if not ptext in dists:
                dists[ptext] = []
            dists[ptext].append(t)
    tester = dists[ptexts[0]]
    for ptext in dists:
        data = dists[ptext]
        print("{0}: {1}, ks test: {2}".format(ptext, scipy.stats.describe(data), scipy.stats.ks_2samp(data, tester, alternative='two-sided', mode='auto').pvalue))
        plt.hist(data)

    
if __name__ == "__main__":
    sweep_score_ctext(10000, "13111d7fe3944a17f307a78b4d2b30c5")
#    explore(10, 500)
#    compare(2000, "0b474a7d75289451531748dc1a221ff9", "d93f6152518ddec1d05c2a83e679340e")
#    timing, counts = sweep_tally_time(10000)
#    save(timing, counts)
#    infinite_encrypt("440ed4556d87501082ab5285c33960c0")

    


    
    




## Send lots of requests -- ranomd
## take the time of each request in like ns or something
## we accumulate total time in a record t[j][b]  j 0..15, byte position in plaintext, b 0.255 byte value at position

## Each request generates 16 different bytes

## 0000xx0000000000000
