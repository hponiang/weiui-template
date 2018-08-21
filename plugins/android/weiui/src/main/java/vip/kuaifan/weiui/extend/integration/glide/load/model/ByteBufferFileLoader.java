package vip.kuaifan.weiui.extend.integration.glide.load.model;

import android.support.annotation.NonNull;
import android.util.Log;
import vip.kuaifan.weiui.extend.integration.glide.Priority;
import vip.kuaifan.weiui.extend.integration.glide.load.DataSource;
import vip.kuaifan.weiui.extend.integration.glide.load.Options;
import vip.kuaifan.weiui.extend.integration.glide.load.data.DataFetcher;
import vip.kuaifan.weiui.extend.integration.glide.signature.ObjectKey;
import vip.kuaifan.weiui.extend.integration.glide.util.ByteBufferUtil;
import vip.kuaifan.weiui.extend.integration.glide.util.Synthetic;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Loads {@link java.nio.ByteBuffer}s using NIO for {@link java.io.File}.
 */
public class ByteBufferFileLoader implements ModelLoader<File, ByteBuffer> {
  private static final String TAG = "ByteBufferFileLoader";

  @Override
  public LoadData<ByteBuffer> buildLoadData(@NonNull File file, int width, int height,
      @NonNull Options options) {
    return new LoadData<>(new ObjectKey(file), new ByteBufferFetcher(file));
  }

  @Override
  public boolean handles(@NonNull File file) {
    return true;
  }

  /**
   * Factory for {@link vip.kuaifan.weiui.extend.integration.glide.load.model.ByteBufferFileLoader}.
   */
  public static class Factory implements ModelLoaderFactory<File, ByteBuffer> {

    @NonNull
    @Override
    public ModelLoader<File, ByteBuffer> build(@NonNull MultiModelLoaderFactory multiFactory) {
      return new ByteBufferFileLoader();
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }

  private static final class ByteBufferFetcher implements DataFetcher<ByteBuffer> {

    private final File file;

    @Synthetic
    @SuppressWarnings("WeakerAccess")
    ByteBufferFetcher(File file) {
      this.file = file;
    }

    @Override
    public void loadData(@NonNull Priority priority,
        @NonNull DataCallback<? super ByteBuffer> callback) {
      ByteBuffer result;
      try {
        result = ByteBufferUtil.fromFile(file);
      } catch (IOException e) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
          Log.d(TAG, "Failed to obtain ByteBuffer for file", e);
        }
        callback.onLoadFailed(e);
        return;
      }

      callback.onDataReady(result);
    }

    @Override
    public void cleanup() {
      // Do nothing.
    }

    @Override
    public void cancel() {
      // Do nothing.
    }

    @NonNull
    @Override
    public Class<ByteBuffer> getDataClass() {
      return ByteBuffer.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
      return DataSource.LOCAL;
    }
  }
}
