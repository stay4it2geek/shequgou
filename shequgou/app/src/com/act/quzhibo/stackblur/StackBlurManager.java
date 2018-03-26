package com.act.quzhibo.stackblur;

import android.content.Context;
import android.graphics.Bitmap;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StackBlurManager {
	static Context _context;
	static final int EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors();
	static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS);


	/**
	 * 原始图像
	 */
	private final Bitmap _image;

	/**
	 * 最近的结果的模糊
	 */
	private Bitmap _result;

	/**
	 * 模糊方式
	 */
	private BlurProcess _blurProcess;

	/**
	 * 构造函数方法（基本初始化和像素数组的构造）
	 * @param context 上下文
	 * @param image 图像将被分析
	 */
	public StackBlurManager(Context context, Bitmap image) {
		_context = context.getApplicationContext();
		_image = image;
	}

	/**
	 * 处理给定半径上的图像。半径必须至少1
	 * @param radius
	 */
	public Bitmap process(int radius) {
		_blurProcess = new JavaBlurProcess();
		_result = _blurProcess.blur(_image, radius);
		return _result;
	}

	/**
	 * 返回作为位图的模糊图像
	 * @return 模糊图像
	 */
	public Bitmap returnBlurredImage() {
		return _result;
	}

	/**
	 * 将图像保存到文件系统中
	 * @param path 保存图像的路径
	 */
	public void saveIntoFile(String path) {
		try {
			FileOutputStream out = new FileOutputStream(path);
			_result.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 返回原始图像作为位图
	 * @return 原始位图图像
	 */
	public Bitmap getImage() {
		return this._image;
	}


	/**
	 *
	 * @param radius 最大模糊度(在0.0到25.0之间)
     * @return
     */
	public Bitmap processRenderScript(float radius) {
		_blurProcess = new RenderScriptBlur(_context);
		_result = _blurProcess.blur(_image,radius);
		return _result;
	}

	public void onDestory(){
		if (_blurProcess != null)
			_blurProcess.onDestory();
	}
}
