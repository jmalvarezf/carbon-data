package es.eci.carbon.datasource.reader.mongo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.utils.CarbonUtils;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import es.eci.carbon.datasource.reader.mongo.config.MongoDataSourceConfiguration;

public class MongoDataSourceReaderUtil {

	public static MongoClient loadConfiguration(String xmlConfiguration) throws DataSourceException {
		ByteArrayInputStream baos = null;
		try {
			xmlConfiguration = CarbonUtils.replaceSystemVariablesInXml(xmlConfiguration);
			JAXBContext ctx = JAXBContext.newInstance(MongoDataSourceConfiguration.class);
			baos = new ByteArrayInputStream(xmlConfiguration.getBytes());
			MongoDataSourceConfiguration fileConfig = (MongoDataSourceConfiguration) ctx.createUnmarshaller()
					.unmarshal(baos);
			MongoClient result = null;
			if (fileConfig.getUrl() != null) {
				MongoClientURI uri = new MongoClientURI(fileConfig.getUrl());
				result = new MongoClient(uri);
			} else {
				List<ServerAddress> addressList = new ArrayList<ServerAddress>();
				if (fileConfig.getReplicaSetConfig() != null) {
					ServerAddress address1 = new ServerAddress(fileConfig.getReplicaSetConfig().getHost1(),
							Integer.parseInt(fileConfig.getReplicaSetConfig().getPort1()));
					addressList.add(address1);
					if (fileConfig.getReplicaSetConfig().getHost2() != null
							&& fileConfig.getReplicaSetConfig().getPort2() != null) {
						ServerAddress address2 = new ServerAddress(fileConfig.getReplicaSetConfig().getHost2(),
								Integer.parseInt(fileConfig.getReplicaSetConfig().getPort2()));
						addressList.add(address2);
					}
					if (fileConfig.getReplicaSetConfig().getHost3() != null
							&& fileConfig.getReplicaSetConfig().getPort3() != null) {
						ServerAddress address3 = new ServerAddress(fileConfig.getReplicaSetConfig().getHost3(),
								Integer.parseInt(fileConfig.getReplicaSetConfig().getPort3()));
						addressList.add(address3);
					}
				} else {
					ServerAddress address = new ServerAddress(fileConfig.getHost(),
							Integer.parseInt(fileConfig.getPort()));
					addressList.add(address);
				}
				MongoClientOptions.Builder builder = MongoClientOptions.builder();
				MongoCredential credential = null;
				if (fileConfig.getWithSSL() != null) {
					builder.sslEnabled(fileConfig.getWithSSL());
				}
				if (fileConfig.getUsername() != null && fileConfig.getPassword() != null
						&& fileConfig.getDatabase() != null) {
					credential = MongoCredential.createCredential(fileConfig.getUsername(), fileConfig.getDatabase(),
							fileConfig.getPassword().toCharArray());
				}
				if (credential != null) {
					result = new MongoClient(addressList, Arrays.asList(new MongoCredential[] { credential }),
							builder.build());
				} else {
					result = new MongoClient(addressList, builder.build());
				}
			}
			return result;
		} catch (Exception e) {
			throw new DataSourceException("Error loading Mongo Datasource configuration: " + e.getMessage(), e);
		} finally {
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException ignore) {
					// ignore
				}
			}
		}
	}

}
