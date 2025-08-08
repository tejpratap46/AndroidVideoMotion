package com.tejpratapsingh.motionlib.pytorch.common

enum class ModelTypes(val fileName: String) {
    U2NET("u2net.ptl"),
    SR3X("super_resolution_3x.ptl"),
    NINASR("ninasr_b0_2x.ptl")
}