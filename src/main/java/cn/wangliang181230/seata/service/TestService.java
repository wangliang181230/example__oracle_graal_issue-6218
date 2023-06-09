package cn.wangliang181230.seata.service;

import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.PostConstruct;

import cn.wangliang181230.seata.domain.TccParam;
import cn.wangliang181230.seata.lang.MyRuntimeException;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestService.class);

	private static final AtomicInteger i = new AtomicInteger(0);


	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ITestTccService tccService;


	@PostConstruct
	public void cleanData() {
		this.clean();
	}

	@GlobalTransactional
	public String test(String test, long sleepTime) throws InterruptedException {
		String xid = RootContext.getXID();
		LOGGER.info("xid: {}", xid);

		if ("0".equals(test)) {
			LOGGER.error("throw error1");
			throw new MyRuntimeException("测试异常情况");
		}

		LOGGER.info("after testError");

		jdbcTemplate.execute("insert into tb_order (id) values (" + i.incrementAndGet() + ")");

		LOGGER.info("after insert");

		if ("1".equals(test)) {
			LOGGER.error("throw error2");
			if (sleepTime > 0) {
				Thread.sleep(sleepTime);
			}
			throw new MyRuntimeException("测试异常情况2");
		}

		LOGGER.info("after testError2");

		tccService.prepare(new TccParam("aaaaaaaaa"));

		LOGGER.info("after tcc prepare");

		if ("2".equals(test)) {
			LOGGER.error("throw error3");
			if (sleepTime > 0) {
				Thread.sleep(sleepTime);
			}
			throw new MyRuntimeException("测试异常情况3");
		}
		LOGGER.info("after testError3");

		return "test";
	}

	public void clean() {
		LOGGER.info("clean tb_order");
		jdbcTemplate.execute("delete from tb_order");
	}

	public int count() {
		return jdbcTemplate.queryForObject("select count(*) from tb_order", Integer.class);
	}

}
