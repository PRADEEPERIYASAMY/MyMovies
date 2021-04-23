package com.example.mymovies.di

import android.content.Context
import android.os.Environment
import com.example.mymovies.cache.MoviesDatabase
import com.github.se_bastiaan.torrentstream.TorrentOptions
import com.github.se_bastiaan.torrentstream.TorrentStream
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService
import com.mhmdawad.torrentmovies.data.source.cache.FavoriteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object AppModule {
    @Provides
    @Singleton
    fun getOpenSubtitleItem(): OpenSubtitlesService {
        return OpenSubtitlesService()
    }

    @Provides
    @Singleton
    fun getExoplayer(@ApplicationContext appContext: Context): SimpleExoPlayer {
        return ExoPlayerFactory.newSimpleInstance(appContext)
    }

    @Provides
    @Singleton
    fun getDefaultDataSourceFactory(@ApplicationContext appContext: Context): DefaultDataSourceFactory {
        return DefaultDataSourceFactory(appContext, Util.getUserAgent(appContext, "MyMovies"), DefaultBandwidthMeter())
    }

    @Provides
    @Singleton
    fun getTorrentOptions(): TorrentOptions {
        return TorrentOptions.Builder().removeFilesAfterStop(true)
            .saveLocation(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
            .build()
    }

    @Provides
    @Singleton
    fun getTorrentStream(torrentOptions: TorrentOptions): TorrentStream {
        return TorrentStream.init(torrentOptions)
    }

    @Provides
    @Singleton
    fun getFormat(): Format {
        return Format.createTextSampleFormat(
            null, MimeTypes.APPLICATION_SUBRIP,
            null, Format.NO_VALUE, Format.NO_VALUE, "en", null, Format.OFFSET_SAMPLE_RELATIVE)
    }
}
