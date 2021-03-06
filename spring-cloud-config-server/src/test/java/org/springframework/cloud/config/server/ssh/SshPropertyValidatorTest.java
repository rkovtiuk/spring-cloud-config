/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.config.server.ssh;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * Unit tests for property based SSH config validators
 *
 * @author Ollie Hughes
 */
public class SshPropertyValidatorTest {

	private static final String SSH_URI = "git@gitserver.com:team/repo1.git";

	private static final String VALID_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\n" +
			"MIIEpAIBAAKCAQEAoqyz6YaYMTr7L8GLPSQpAQXaM04gRx4CCsGK2kfLQdw4BlqI\n" +
			"yyxp38YcuZG9cUDBAxby+K2TKmwHaC1R61QTwbPuCRdIPrDwRz+FLoegm3iDLCmn\n" +
			"uP6rjZDneYsqfU1KSdrOwIbCnONfDdvYL/vnZC/o8DDMlk5Orw2SfHkT3pq0o8km\n" +
			"ayBwN4Sf3bpyWTY0oZcmNeSCCoIdE59k8Pa7/t9bwY9caLj05C3DEsjucc7Ei/Eq\n" +
			"TOyGyobtXwaya5CqKLUHes74Poz1aEP/yVFdUud91uezd8ZK1P1t5/ZKA3R6aHir\n" +
			"+diDJ2/GQ2tD511FW46yw+EtBUJTO6ADVv4UnQIDAQABAoIBAF+5qwEfX82QfKFk\n" +
			"jfADqFFexUDtl1biFKeJrpC2MKhn01wByH9uejrhFKQqW8UaKroLthyZ34DWIyGt\n" +
			"lDnHGv0gSVF2LuAdNLdobJGt49e4+c9yD61vxzm97Eh8mRs08SM2q/VlF35E2fmI\n" +
			"xdWusUImYzd8L9e+6tRd8zZl9UhG5vR5XIstKqxC6S0g79aAt0hasE4Gw1FKOf2V\n" +
			"4mlL15atjQSKCPdOicuyc4zpjAtU1A9AfF51iG8oOUuJebPW8tCftfOQxaeGFgMG\n" +
			"7M9aai1KzXR6M5IBAKEv31yBvz/SHTneP7oZXNLeC1GIR420PKybmeZdNK8BbEAu\n" +
			"3reKgm0CgYEA03Sx8JoF5UBsIvFPpP1fjSlTgKryM5EJR6KQtj5e4YfyxccJepN8\n" +
			"q4MrqDfNKleG/a1acEtDMhBNovU7Usp2QIP7zpAeioHBOhmE5WSieZGc3icOGWWq\n" +
			"mRkdulSONruqWKv76ZoluxftekE03bDhZDNlcCgmrslEKB/ufHd2oc8CgYEAxPFa\n" +
			"lKOdSeiYFV5CtvO8Ro8em6rGpSsVz4qkPxbeBqUDCb9KXHhq6YrhRxOIfQJKfT7M\n" +
			"ZFCn8ArJXKgOGu+KsvwIErFHF9g2jJMG4DOUTpkQgi2yveihFxcmz/AltyVXgrnv\n" +
			"ZWQbAerH77pdKKhNivLGgEv72GYawdYjYNjemdMCgYA2kEMmMahZyrDcp2YEzfit\n" +
			"BT/t0K6kzcUWPgWXcSqsiZcEn+J7RbmCzFskkhmX1nQX23adyV3yejB+X0dKisHO\n" +
			"zf/ZAmlPFkJVCqa3RquCMSfIT02dEhXeYZPBM/Zqeyxuqxpa4hLgX0FBLbhFiFHw\n" +
			"uC5xrXql2XuD2xF//peXEwKBgQC+pa28Cg7vRxxCQzduB9CQtWc55j3aEjVQ7bNF\n" +
			"54sS/5ZLT0Ra8677WZfuyDfuW9NkHvCZg4Ku2qJG8eCFrrGjxlrCTZ62tHVJ6+JS\n" +
			"E1xUIdRbUIWhVZrr0VufG6hG/P0T7Y6Tpi6G0pKtvMkF3LcD9TS3adboix8H2ZXx\n" +
			"4L7MRQKBgQC0OO3qqNXOjIVYWOoqXLybOY/Wqu9lxCAgGyCYaMcstnBI7W0MZTBr\n" +
			"/syluvGsaFc1sE7MMGOOzKi1tF4YvDmSnzA/R1nmaPguuD9fOA+w7Pwkv5vLvuJq\n" +
			"2U7EeNwxq1I1L3Ag6E7wH4BHLHd4TKaZR6agFkn8oomz71yZPGjuZQ==\n" +
			"-----END RSA PRIVATE KEY-----";

	private static final String VALID_HOST_KEY = "AAAAB3NzaC1yc2EAAAADAQABAAABAQDg6/W/5cbk/npvzpae7ZEa54F4rkwh2V3NiuqVZ5hWr+8O4/6SmrS7yBvRHAFeAJNb0LOCjE/7tjd1fqUx+QU1ATCtwkOhuwG8Ubzkx23mMZlrwEvx7XEfBoLN7Lw9fXjWDtTTgFB1AxCQ2pGGiNG0QCwyA4HViDHVU+ibwkRlzuDJG0tnp5Qpo3DXkHwFNdqWNfVrIZ6q2xbyeoJjKjnR215T0ehmuWFmKqG+uMNe/LQ6IOiK0F5+gr7rgPxNLAYYqyhraAnBeHn5gapsSzYJmFpoAHWvN7OUwHcJ88D9qUkKi4VKxYiuK69u3z825Xj2cLTfj9JiHCfV8cTo9GL";
	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void supportedParametersSuccesful() throws Exception {
		SshUriProperties validSettings = SshUri.builder()
				.uri(SSH_URI)
				.ignoreLocalSshSettings(true)
				.privateKey(VALID_PRIVATE_KEY)
				.hostKey(VALID_HOST_KEY)
				.hostKeyAlgorithm("ssh-rsa")
				.build();

		Set<ConstraintViolation<SshUriProperties>> constraintViolations = validator.validate(validSettings);
		assertThat(constraintViolations, hasSize(0));

	}

	@Test
	public void invalidPrivateKeyFails() throws Exception {

		SshUriProperties invalidKey = SshUri.builder()
				.uri(SSH_URI)
				.ignoreLocalSshSettings(true)
				.privateKey("invalid_key")
				.build();

		Set<ConstraintViolation<SshUriProperties>> constraintViolations = validator.validate(invalidKey);
		assertThat(constraintViolations, hasSize(1));

	}

	@Test
	public void missingPrivateKeyFails() throws Exception {

		SshUriProperties missingKey = SshUri.builder()
				.uri(SSH_URI)
				.ignoreLocalSshSettings(true)
				.build();

		Set<ConstraintViolation<SshUriProperties>> constraintViolations = validator.validate(missingKey);
		assertThat(constraintViolations, hasSize(1));
	}

	@Test
	public void hostKeyWithMissingAlgoFails() throws Exception {

		SshUriProperties missingAlgo = SshUri.builder()
				.uri(SSH_URI)
				.ignoreLocalSshSettings(true)
				.privateKey(VALID_PRIVATE_KEY)
				.hostKey("some_host")
				.build();

		Set<ConstraintViolation<SshUriProperties>> constraintViolations = validator.validate(missingAlgo);
		assertThat(constraintViolations, hasSize(1));
	}

	@Test
	public void algoWithMissingHostKeyFails() throws Exception {

		SshUriProperties missingHostKey = SshUri.builder()
				.uri(SSH_URI)
				.ignoreLocalSshSettings(true)
				.privateKey(VALID_PRIVATE_KEY)
				.hostKeyAlgorithm("ssh-rsa")
				.build();

		Set<ConstraintViolation<SshUriProperties>> constraintViolations = validator.validate(missingHostKey);
		assertThat(constraintViolations, hasSize(1));
	}

	@Test
	public void unsupportedAlgoFails() throws Exception {

		SshUriProperties unsupportedAlgo = SshUri.builder()
				.uri(SSH_URI)
				.ignoreLocalSshSettings(true)
				.privateKey(VALID_PRIVATE_KEY)
				.hostKey("some_host_key")
				.hostKeyAlgorithm("unsupported")
				.build();

		Set<ConstraintViolation<SshUriProperties>> constraintViolations = validator.validate(unsupportedAlgo);
		assertThat(constraintViolations, hasSize(1));
	}

	@Test
	public void validatorNotRunIfIgnoreLocalSettingsFalse() throws Exception {

		SshUriProperties useLocal = (SshUri.builder()
				.uri(SSH_URI)
				.ignoreLocalSshSettings(false)
				.privateKey("invalid_key")
				.build());

		Set<ConstraintViolation<SshUriProperties>> constraintViolations = validator.validate(useLocal);
		assertThat(constraintViolations, hasSize(0));

	}

	@Test
	public void validatorNotRunIfHttpsUri() throws Exception {

		SshUriProperties httpsUri = (SshUri.builder()
				.uri("https://somerepo.com/team/project.git")
				.ignoreLocalSshSettings(true)
				.privateKey("invalid_key")
				.build());

		Set<ConstraintViolation<SshUriProperties>> constraintViolations = validator.validate(httpsUri);
		assertThat(constraintViolations, hasSize(0));

	}
}