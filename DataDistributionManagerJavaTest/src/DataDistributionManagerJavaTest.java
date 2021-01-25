/*
*  Copyright 2021 MASES s.r.l.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*  Refer to LICENSE for more information.
*/

import java.nio.charset.Charset;

import org.mases.businesscontinuity.*;

public class DataDistributionManagerJavaTest {
	public static void main(String args[]) {
		final int THRESHOLD = 1000;

		DDM_TOPIC_DIRECTION direction = DDM_TOPIC_DIRECTION.RECEIVER;

		MySmartDataDistribution dataDistribution = new MySmartDataDistribution();
		String str = "prova";
		HRESULT hRes = dataDistribution.Initialize(
				"D:/MARIO/MASES/BusinessContinuityApplication/Configuration/OpenDDSManager.conf", str, "KafkaManager");

		if (hRes.getFailed()) {
			System.out.println("Error in configuration.");
			return;
		}

		hRes = dataDistribution.Start(10000);

		if (hRes.getFailed()) {
			return;
		}

		MySmartDataDistributionTopic provaTopic;
		try {
			provaTopic = dataDistribution.CeateSmartTopic(MySmartDataDistributionTopic.class, "prova",
					DDM_TOPIC_DIRECTION.RECEIVER, null);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}

		System.out.println("After StartMasterConsumerAndWait...\n");

		provaTopic.StartTopic(10000);

		try {
			System.out.println("Starting sending...\n");

			int counter = 100;

			byte[] buffer = str.getBytes(Charset.forName("ASCII"));
			while (true) {
				hRes = HRESULT.S_OK;
				if (direction == DDM_TOPIC_DIRECTION.TRANSMITTER) {
					hRes = provaTopic.WriteOnTopic(null, buffer, false, -1);
				}
				if (hRes == HRESULT.S_OK) {
					str = String.format("{0:10}", counter++);
					buffer = str.getBytes(Charset.forName("ASCII"));
					if ((counter % THRESHOLD) == 0)
						System.out.println(String.format("SendData Reached {0}", counter));
				}
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}