package com.parkeye.pvs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class PvsApplication {

	/** Parking times are local to Strumica; all LocalDateTime columns and "now" use this zone. */
	private static final String APP_TIMEZONE = "Europe/Skopje";

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(APP_TIMEZONE));
		SpringApplication.run(PvsApplication.class, args);
	}

	/** Single time source - inject this instead of calling LocalDateTime.now() (also makes tests easy). */
	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}
}
