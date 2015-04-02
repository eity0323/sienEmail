package mi.email.way2.control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

public class Helper {
	/**
	 * 保存文件内容
	 * 
	 * @param fileName
	 *            文件名
	 * @param input
	 *            输入流
	 * @throws IOException
	 */
	public static void saveFile(String fileName, Reader input) throws IOException {

		// 为了放置文件名重名，在重名的文件名后面天上数字
		File file = new File(fileName);
		if(file.exists())	return;
		
		// 先取得文件名的后缀
		int lastDot = fileName.lastIndexOf(".");
		String extension = fileName.substring(lastDot);
		fileName = fileName.substring(0, lastDot);
		for (int i = 0; file.exists(); i++) {
			// 　如果文件重名，则添加i
			file = new File(fileName + i + extension);
		}
		// 从输入流中读取数据，写入文件输出流
		FileWriter fos = new FileWriter(file);
		BufferedWriter bos = new BufferedWriter(fos);
		BufferedReader bis = new BufferedReader(input);
		int aByte;
		while ((aByte = bis.read()) != -1) {
			bos.write(aByte);
		}
		// 关闭流
		bos.flush();
		bos.close();
		bis.close();
	}
}
