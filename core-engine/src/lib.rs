use jni::objects::{JClass, JObjectArray, JString};
use jni::sys::{jfloat, jfloatArray, jint, jintArray, jsize};
use jni::JNIEnv;

/// Computes a fuzzy matching score between a query and a target label.
/// Higher score means a better match.
/// Returns None if there is no viable match.
pub fn fuzzy_score(query: &str, target: &str) -> Option<i32> {
    let q = query.trim().to_lowercase();
    let t = target.trim().to_lowercase();

    if q.is_empty() {
        return Some(0);
    }
    if t.is_empty() {
        return None;
    }

    // 1. Exact match
    if t == q {
        return Some(10000);
    }

    // 2. Exact prefix
    if t.starts_with(&q) {
        return Some(5000 + (100 - t.len().min(100) as i32));
    }

    // 3. Acronym / Initials match (e.g. "yt" -> "YouTube", "ps" -> "Play Store")
    let words: Vec<&str> = t.split(|c: char| !c.is_alphanumeric() || c.is_whitespace()).filter(|w| !w.is_empty()).collect();
    let initials: String = words.iter().filter_map(|w| w.chars().next()).collect();
    if initials.starts_with(&q) {
        return Some(4000 + (100 - words.len().min(100) as i32) * 10);
    }

    // 4. Word boundary prefix (e.g. "tube" in "You Tube")
    for word in &words {
        if word.starts_with(&q) {
            return Some(3000 + (100 - word.len().min(100) as i32));
        }
    }

    // 5. Substring match
    if let Some(pos) = t.find(&q) {
        return Some(2000 - pos as i32);
    }

    // 6. Subsequence matching
    let mut q_chars = q.chars().peekable();
    let mut score = 1000;
    let mut consecutive = 0;

    for ch in t.chars() {
        if let Some(&target_ch) = q_chars.peek() {
            if ch == target_ch {
                q_chars.next();
                consecutive += 1;
                score += consecutive * 15;
            } else {
                consecutive = 0;
                score -= 1;
            }
        }
    }

    if q_chars.peek().is_none() {
        Some(score.max(1))
    } else {
        None
    }
}

/// Calculate ergonomic wave offsets for the alphabet touch scrubber.
/// Returns an array of horizontal (x) offset multipliers for each letter item.
pub fn calculate_wave(selected_index: f32, total_count: usize, wave_radius: f32, max_offset_px: f32) -> Vec<f32> {
    let mut offsets = Vec::with_capacity(total_count);
    let radius = wave_radius.max(1.0);

    for i in 0..total_count {
        let diff = (i as f32 - selected_index).abs();
        if diff > radius {
            offsets.push(0.0);
        } else {
            // Smooth bell curve (cosine distribution)
            let factor = (std::f32::consts::PI * diff / (radius * 2.0)).cos();
            let curved = factor * factor; // smooth damping
            offsets.push(curved * max_offset_px);
        }
    }
    offsets
}

// -----------------------------------------------------------------------------
// JNI Exports for Android Kotlin NativeEngine
// Package: com.olens.launcher.core.NativeEngine
// -----------------------------------------------------------------------------

#[no_mangle]
pub extern "system" fn Java_com_olens_launcher_core_NativeEngine_nativeCalculateWaveOffsets(
    env: JNIEnv,
    _class: JClass,
    selected_index: jfloat,
    total_count: jint,
    wave_radius: jfloat,
    max_offset: jfloat,
) -> jfloatArray {
    let count = total_count.max(0) as usize;
    let offsets = calculate_wave(selected_index as f32, count, wave_radius as f32, max_offset as f32);

    match env.new_float_array(count as jsize) {
        Ok(arr) => {
            if let Err(e) = env.set_float_array_region(&arr, 0, &offsets) {
                eprintln!("Failed to set wave region: {:?}", e);
            }
            arr.into_raw()
        }
        Err(_) => std::ptr::null_mut(),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_olens_launcher_core_NativeEngine_nativeFuzzySearch(
    mut env: JNIEnv,
    _class: JClass,
    query_j: JString,
    labels_j: JObjectArray,
    packages_j: JObjectArray,
) -> jintArray {
    let query: String = match env.get_string(&query_j) {
        Ok(s) => s.into(),
        Err(_) => return std::ptr::null_mut(),
    };

    let len = match env.get_array_length(&labels_j) {
        Ok(l) => l as usize,
        Err(_) => return std::ptr::null_mut(),
    };

    let mut scored: Vec<(usize, i32)> = Vec::with_capacity(len);

    for i in 0..len {
        let label_obj = match env.get_object_array_element(&labels_j, i as jsize) {
            Ok(o) => o,
            Err(_) => continue,
        };
        let label_str: JString = label_obj.into();
        let label: String = match env.get_string(&label_str) {
            Ok(s) => s.into(),
            Err(_) => continue,
        };

        if let Some(score) = fuzzy_score(&query, &label) {
            scored.push((i, score));
            continue;
        }

        // Fallback: check package name if available
        let pkg_opt: Option<String> = env
            .get_object_array_element(&packages_j, i as jsize)
            .ok()
            .and_then(|obj| {
                let pkg_str = JString::from(obj);
                env.get_string(&pkg_str).ok().map(|s| s.into())
            });

        if let Some(pkg_name) = pkg_opt {
            if let Some(score) = fuzzy_score(&query, &pkg_name) {
                scored.push((i, score / 2));
            }
        }
    }

    // Sort by descending score
    scored.sort_by(|a, b| b.1.cmp(&a.1));

    let result_indices: Vec<jint> = scored.into_iter().map(|(idx, _)| idx as jint).collect();

    match env.new_int_array(result_indices.len() as jsize) {
        Ok(arr) => {
            let _ = env.set_int_array_region(&arr, 0, &result_indices);
            arr.into_raw()
        }
        Err(_) => std::ptr::null_mut(),
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_fuzzy_score() {
        assert!(fuzzy_score("yt", "YouTube").is_some());
        assert!(fuzzy_score("cam", "Camera").is_some());
        assert!(fuzzy_score("chrome", "Google Chrome").is_some());
        let s_exact = fuzzy_score("camera", "camera").unwrap();
        let s_prefix = fuzzy_score("cam", "camera").unwrap();
        assert!(s_exact > s_prefix);
    }

    #[test]
    fn test_wave_calculation() {
        let offsets = calculate_wave(5.0, 26, 3.0, 48.0);
        assert_eq!(offsets.len(), 26);
        assert!(offsets[5] > offsets[4]);
        assert!(offsets[5] > offsets[6]);
        assert_eq!(offsets[0], 0.0);
    }
}

