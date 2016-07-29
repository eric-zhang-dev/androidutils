package com.zy.androidutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FiltieUl {
	public static void mkFile(String filePath, boolean mkdir) throws Exception {
		File file = new File(filePath);
		file.getParentFile().mkdirs();
		file.createNewFile();
		file = null;
	}

	/**
	 * ��ָ����λ�ô����ļ���
	 * 
	 * @param dirPath
	 *            �ļ���·��
	 * @return �������ɹ����򷵻�True����֮���򷵻�False
	 */
	public static boolean mkDir(String dirPath) {
		return new File(dirPath).mkdirs();
	}

	/**
	 * ɾ��ָ�����ļ�
	 * 
	 * @param filePath
	 *            �ļ�·��
	 * 
	 * @return ��ɾ���ɹ����򷵻�True����֮���򷵻�False
	 * 
	 */
	public static boolean delFile(String filePath) {
		return new File(filePath).delete();
	}

	/**
	 * ɾ��ָ�����ļ���
	 * 
	 * @param dirPath
	 *            �ļ���·��
	 * @param delFile
	 *            �ļ������Ƿ�����ļ�
	 * @return ��ɾ���ɹ����򷵻�True����֮���򷵻�False
	 * 
	 */
	public static boolean delDir(String dirPath, boolean delFile) {
		if (delFile) {
			File file = new File(dirPath);
			if (file.isFile()) {
				return file.delete();
			} else if (file.isDirectory()) {
				if (file.listFiles().length == 0) {
					return file.delete();
				} else {
					int zfiles = file.listFiles().length;
					File[] delfile = file.listFiles();
					for (int i = 0; i < zfiles; i++) {
						if (delfile[i].isDirectory()) {
							delDir(delfile[i].getAbsolutePath(), true);
						}
						delfile[i].delete();
					}
					return file.delete();
				}
			} else {
				return false;
			}
		} else {
			return new File(dirPath).delete();
		}
	}

	/**
	 * �����ļ�/�ļ��� ��Ҫ�����ļ��и��ƣ�����Ŀ���ļ�������Դ�ļ�����
	 * 
	 * @param source
	 *            Դ�ļ����У�
	 * @param target
	 *            Ŀ���ļ����У�
	 * @param isFolder
	 *            �������ļ��и��ƣ���ΪTrue����֮ΪFalse
	 * @throws Exception
	 */
	public static void copy(String source, String target, boolean isFolder)
			throws Exception {
		if (isFolder) {
			(new File(target)).mkdirs();
			File a = new File(source);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (source.endsWith(File.separator)) {
					temp = new File(source + file[i]);
				} else {
					temp = new File(source + File.separator + file[i]);
				}
				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(target + "/"
							+ (temp.getName()).toString());
					byte[] b = new byte[1024];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {
					copy(source + "/" + file[i], target + "/" + file[i], true);
				}
			}
		} else {
			int byteread = 0;
			File oldfile = new File(source);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(source);
				File file = new File(target);
				file.getParentFile().mkdirs();
				file.createNewFile();
				FileOutputStream fs = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}
		}
	}

	/**
	 * �ƶ�ָ�����ļ����У���Ŀ���ļ����У�
	 * 
	 * @param source
	 *            Դ�ļ����У�
	 * @param target
	 *            Ŀ���ļ����У�
	 * @param isFolder
	 *            ��Ϊ�ļ��У���ΪTrue����֮ΪFalse
	 * @return
	 * @throws Exception
	 */
	public static boolean move(String source, String target, boolean isFolder)
			throws Exception {
		copy(source, target, isFolder);
		if (isFolder) {
			return delDir(source, true);
		} else {
			return delFile(source);
		}
	}
}
