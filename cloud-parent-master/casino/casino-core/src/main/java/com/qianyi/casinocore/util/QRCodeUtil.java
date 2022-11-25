package com.qianyi.casinocore.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.netty.util.internal.StringUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * 生成二维码
 *
 * @Description TDD
 * @Author Owner
 */
public class QRCodeUtil {

	/**
	 * 获取二维码base64
	 *
	 * @param url
	 * @param width
	 * @param height
	 * @return
	 * @throws WriterException
	 * @throws IOException
	 */
	public static String getQRCodeImageByBase64(String url, Integer width, Integer height) throws WriterException,
        IOException {
		if (StringUtil.isNullOrEmpty(url)){
			return "";
		}

		if (width == null) {
			width = 350;
		}
		if (height == null) {
			height = 350;
		}

		QRCodeWriter qrCodeWriter = new QRCodeWriter();

		BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, width, height);

		BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "png", os);

		return Base64.getEncoder().encodeToString(os.toByteArray());
	}
}
