/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;


@Controller
public class VideoController {

	private Map<Long,Video> videoMap= new HashMap<Long, Video>();
	private static final AtomicLong currentId = new AtomicLong(0L);
	private VideoFileManager videoDataMgr;
	
	@RequestMapping(value = "/video", method = RequestMethod.POST)
	@ResponseBody
	public Video saveVideoMetaData(@RequestBody Video video){
		return save(video);
	}
	
	@RequestMapping(value = "/video", method = RequestMethod.GET)
	@ResponseBody
	public Collection<Video> getVideoMetaData(){
		return videoMap.values();
	}
	
	@RequestMapping(value = "/video/{id}/data", method = RequestMethod.POST)
	@ResponseBody
	public VideoStatus saveVideoData(@PathVariable("id") long id, @RequestParam("data") MultipartFile videoData) throws IOException{
		VideoStatus videoStatus = new VideoStatus(VideoState.READY);
		if(videoMap.containsKey(id)){
			videoDataMgr = VideoFileManager.get();
			Video video = videoMap.get(id);
			videoDataMgr.saveVideoData(video, videoData.getInputStream());
		}else{
			throw new ResourceNotFoundException(); 
		}
		return videoStatus;
	}
	
	@RequestMapping(value = "/video/{id}/data", method = RequestMethod.GET)
	public void getVideoData(@PathVariable("id") long id, HttpServletResponse response) throws IOException{
		if(videoMap.containsKey(id)){
			videoDataMgr = VideoFileManager.get();
			Video video = videoMap.get(id);
			videoDataMgr.copyVideoData(video, response.getOutputStream());
		}else{
			throw new ResourceNotFoundException(); 
		}
		
	}

	private Video save(Video video) {
		video.setId(setId(video));
		video.setDataUrl(getDataUrl(video.getId()));
		videoMap.put(video.getId(), video);
		return video;		
	}

	private String getDataUrl(long id) {
		String url = getUrlBaseForLocalServer() + "/video/" + id + "/data";
        return url;		
	}
	
	private String getUrlBaseForLocalServer() {
		   HttpServletRequest request = 
		       ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		   String base = 
		      "http://"+request.getServerName() 
		      + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
		   return base;
		}

	private long setId(Video video) {
		// TODO Auto-generated method stub
		if(video.getId() == 0){
			return currentId.incrementAndGet();
		}
		return(video.getId());
	}
	
}
