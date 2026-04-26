# MotionLib Easings

This module provides a wide variety of easing functions to create smooth and natural animations.

## Easing Types

The following easing types are supported:

| Category | Easings |
| --- | --- |
| **Linear** | `LINEAR` |
| **Sine** | `SIN_IN`, `SIN_OUT`, `SIN_IN_OUT` |
| **Quad** | `QUAD_IN`, `QUAD_OUT`, `QUAD_IN_OUT` |
| **Cubic** | `CUBIC_IN`, `CUBIC_OUT`, `CUBIC_IN_OUT` |
| **Quart** | `QUART_IN`, `QUART_OUT`, `QUART_IN_OUT` |
| **Quint** | `QUINT_IN`, `QUINT_OUT`, `QUINT_IN_OUT` |
| **Exponential** | `EXP_IN`, `EXP_OUT`, `EXP_IN_OUT` |
| **Circular** | `CIRC_IN`, `CIRC_OUT`, `CIRC_IN_OUT` |
| **Back** | `BACK_IN`, `BACK_OUT`, `BACK_IN_OUT` |
| **Elastic** | `ELASTIC_IN`, `ELASTIC_OUT`, `ELASTIC_IN_OUT` |
| **Bounce** | `BOUNCE_IN`, `BOUNCE_OUT`, `BOUNCE_IN_OUT` |

## Easing Graphs

Below is a visual representation of how these easing functions behave:

![Easing Graphs](../../docs/img.png)

## Usage

You can use these easings by passing the `Easings` enum to the `Interpolators` class:

```kotlin
val interpolator = Interpolators(Easings.CUBIC_IN_OUT)
```
