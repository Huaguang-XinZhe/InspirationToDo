package com.liuzhihui.cao.utils

import android.media.AudioAttributes
import android.media.SoundPool
import com.liuzhihui.cao.R
import org.litepal.LitePalApplication

/**
 * 创建 SoundPool 对象
 */
fun createSoundPool(): SoundPool {
    val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    return SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(attributes)
        .build()
}


/**
 * 加载 complete 音频文件
 */
fun SoundPool.load() = load(LitePalApplication.getContext(), R.raw.complete, 1)

/**
 * 自动暂停，卸载加载的 complete 音频文件并释放内存呢资源
 */
fun SoundPool.releaseAndUnload(soundId: Int) {
    autoPause()
    unload(soundId)
    release()
}