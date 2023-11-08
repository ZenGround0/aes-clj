package cacheclear

import (
	"math/rand"
)

// system_profiler SPHardwareDataType;
// ... L2 Cache (per Core): 512 KB
// ... L3 Cache: 6 MB
// sysctl -a | grep hw.l1
// hw.l1icachesize: 32768
// hw.l1dcachesize: 49152

const L1Size = 60_000 // > 49k, < 512M
const L2Size = 600_000// > 512M, < 6M
const L3Size = 12_000_000 // > 6M


var L1 [L1Size]int32
var L2 [L2Size]int32
var L3 [L3Size]int32

// re-implement math/rand.Perm for fun
func RandPerm(arr []int32) {
	size := len(arr)
	// Init to all indexes
	for i := 0; i < size; i++ {
		arr[i] = int32(i)
	}
	// Fisher yates shuffle for unpredictable access pattern
	for i := 0; i < size; i++ {
		j := int32(rand.Intn(size - i))
		temp := arr[int(i)]
		arr[int(i)] = arr[int(j)]
		arr[int(j)] = temp
	}
}

func FollowPerm(arr []int32) int32 {
	idx := int32(rand.Intn(len(arr)))
	for i := 0; i < len(arr); i++ {
		idx = arr[int(idx)]
	}
	return idx
}

func init() {
	RandPerm(L1[:])
	RandPerm(L2[:])
	RandPerm(L3[:])
}

func ClearL1() {
	FollowPerm(L1[:])
}

func ClearL2() {
	FollowPerm(L2[:])
}

func ClearL3() {
	FollowPerm(L3[:])
}
