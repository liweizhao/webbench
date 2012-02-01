package com.netease.webbench.test.common;

import java.util.Arrays;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.UnitTestHelper;
import com.netease.webbench.common.DynamicArray;
import com.netease.webbench.random.ZipfGenerator;

public class ZipfGeneratorTest extends TestCase {

	public ZipfGeneratorTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetZipfRandomNum() {
		try {
			BbTestOptions bbTestOpt = UnitTestHelper.createDflBbTestOpt();
			bbTestOpt.setTbSize(1000000);
			ZipfGenerator zg = new ZipfGenerator(bbTestOpt.getBlgZipfPct(), bbTestOpt.getBlgZipfRes(), bbTestOpt.getBlgZipfPart(), bbTestOpt.getTbSize(), false, true);
			
			int testTimes = 10000000;
			long min = bbTestOpt.getTbSize(), max = 0;

			DynamicArray<Integer> idHitCounts = new DynamicArray<Integer>(bbTestOpt.getTbSize());// 用于记录每个id被选中的次数
			for (int i = 0; i < bbTestOpt.getTbSize(); i++)
				idHitCounts.append(0);

			for (int i = 0; i < testTimes; i++) {
				long n = zg.getZipfRandomNum();
				assertTrue(n <= bbTestOpt.getTbSize());
				int old = idHitCounts.get(n - 1);
				idHitCounts.set(n - 1, old + 1);
				if (n < min)
					min = n;
				if (n > max)
					max = n;
			}

			int[] arr = new int[(int) bbTestOpt.getTbSize()];
			for (int i = 0; i < bbTestOpt.getTbSize(); i++)
				arr[i] = idHitCounts.get(i);

			Arrays.sort(arr);

			long total5pct = 0;//前BlgZipfPct%记录的命中次数统计
			int pct5 = (int) ((bbTestOpt.getTbSize()) * bbTestOpt.getBlgZipfPct() / 100.0);
			for (int i = (int) (bbTestOpt.getTbSize()), count = 1; count <= pct5; count++)
				total5pct += arr[i - count];

			System.out.println("min: " + min + ", max: " + max + ", total: "
					+ testTimes + ",total5pct: " + total5pct);
			
			double prob = total5pct * 100.0 / testTimes; //实际概率最大的前5%记录的概率总和
			
			assertTrue(prob > bbTestOpt.getBlgZipfRes() * 0.98
					&& prob < bbTestOpt.getBlgZipfRes() * 1.02);

			assertTrue(min < 10 && min > 0);
			assertTrue(max > bbTestOpt.getTbSize() * 0.99
					&& max < bbTestOpt.getTbSize() * 1.01);
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testUpdateProb() {
		try {
			BbTestOptions bbTestOpt = UnitTestHelper.createDflBbTestOpt();
			bbTestOpt.setTbSize(1000000);
			ZipfGenerator zg = new ZipfGenerator(bbTestOpt.getBlgZipfPct(), bbTestOpt.getBlgZipfRes(), bbTestOpt.getBlgZipfPart(), bbTestOpt.getTbSize(), false, true);

			//动态插入100万条记录
			int dyInsert = 1000000;
			for (int i = 0; i < dyInsert; i++)
				zg.updateProb();
			
			bbTestOpt.setTbSize(bbTestOpt.getTbSize() + dyInsert);
			
			int testTimes = 10000000;
			long min = bbTestOpt.getTbSize(), max = 0;

			DynamicArray<Integer> idHitCounts = new DynamicArray<Integer>(bbTestOpt.getTbSize());// 用于记录每个id被选中的次数
			for (int i = 0; i < bbTestOpt.getTbSize(); i++)
				idHitCounts.append(0);

			for (int i = 0; i < testTimes; i++) {
				long n = zg.getZipfRandomNum();
				assertTrue(n <= bbTestOpt.getTbSize());
				int old = idHitCounts.get(n - 1);
				idHitCounts.set(n - 1, old + 1);
				if (n < min)
					min = n;
				if (n > max)
					max = n;
			}

			int[] arr = new int[(int) bbTestOpt.getTbSize()];
			for (int i = 0; i < bbTestOpt.getTbSize(); i++)
				arr[i] = idHitCounts.get(i);

			Arrays.sort(arr);

			long total5pct = 0;//前BlgZipfPct%记录的命中次数统计
			int pct5 = (int) ((bbTestOpt.getTbSize()) * bbTestOpt.getBlgZipfPct() / 100.0);
			for (int i = (int) (bbTestOpt.getTbSize()), count = 1; count <= pct5; count++)
				total5pct += arr[i - count];

			System.out.println("min: " + min + ", max: " + max + ", total: "
					+ testTimes + ",total5pct: " + total5pct);
			
			double prob = total5pct * 100.0 / testTimes; //实际概率最大的前5%记录的概率总和
			
			assertTrue(prob > bbTestOpt.getBlgZipfRes() * 0.98
					&& prob < bbTestOpt.getBlgZipfRes() * 1.02);

			assertTrue(min < 10 && min > 0);
			assertTrue(max > bbTestOpt.getTbSize() * 0.99
					&& max < bbTestOpt.getTbSize() * 1.01);
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}	
	}
}
