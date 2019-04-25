package com.sse.attemper.core.controller.job;

import com.sse.attemper.common.constant.APIPath;
import com.sse.attemper.common.param.dispatch.job.JobArgListParam;
import com.sse.attemper.common.param.dispatch.job.JobGetParam;
import com.sse.attemper.common.param.dispatch.job.JobListParam;
import com.sse.attemper.common.param.dispatch.job.JobProjectSaveParam;
import com.sse.attemper.common.result.CommonResult;
import com.sse.attemper.common.result.dispatch.job.FlowJob;
import com.sse.attemper.common.result.dispatch.project.Project;
import com.sse.attemper.core.service.job.JobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author ldang
 */
@Api("Job")
@RestController
public class JobController {
	
	@Autowired
	private JobService service;

	@ApiOperation("List jobs")
	@ApiImplicitParam(value = "JobListParam", name = "param", dataType = "JobListParam", required = true)
	@GetMapping(APIPath.JobPath.LIST)
	public CommonResult<Map<String, Object>> list(JobListParam param) {
		return CommonResult.putResult(service.list(param));
	}

	@ApiOperation("Get job")
	@ApiImplicitParam(value = "JobGetParam", name = "param", dataType = "JobGetParam", required = true)
	@GetMapping(APIPath.JobPath.GET)
	public CommonResult<FlowJob> get(JobGetParam param) {
		return CommonResult.putResult(service.get(param));
	}

	@ApiOperation("List jobs by versions")
	@ApiImplicitParam(value = "JobGetParam", name = "param", dataType = "JobGetParam", required = true)
	@GetMapping(APIPath.JobPath.VERSIONS)
	public CommonResult<List<FlowJob>> listVersions(JobGetParam param) {
		return CommonResult.putResult(service.versions(param));
	}

	@ApiOperation("List args of job")
	@ApiImplicitParam(value = "JobArgListParam", name = "param", dataType = "JobArgListParam", required = true)
	@GetMapping(APIPath.JobPath.LIST_ARG)
	public CommonResult<Map<String, Object>> listArg(JobArgListParam param) {
		return CommonResult.putResult(service.listArg(param));
	}

	@ApiOperation("Get project")
	@ApiImplicitParam(value = "JobGetParam", name = "param", dataType = "JobGetParam", required = true)
	@GetMapping(APIPath.JobPath.GET_PROJECT)
	public CommonResult<Project> getProject(JobGetParam param) {
		return CommonResult.putResult(service.getProject(param));
	}

	@ApiOperation("Save project of job")
	@ApiImplicitParam(value = "JobProjectSaveParam", name = "param", dataType = "JobProjectSaveParam", required = true)
	@PutMapping(APIPath.JobPath.UPDATE_PROJECT)
	public CommonResult<Void> saveProject(@RequestBody JobProjectSaveParam param) {
		return CommonResult.putResult(service.saveProject(param));
	}
}