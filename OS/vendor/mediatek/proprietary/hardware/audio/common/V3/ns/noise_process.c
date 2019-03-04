#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

#define LOG_TAG "noise_suppression"

#define DR_MP3_IMPLEMENTATION

#include "dr_mp3.h"

#define DR_WAV_IMPLEMENTATION

#include "dr_wav.h"
#include "timing.h"

#include "noise_suppression.h"
#include "noise_process.h"

#include <utils/Log.h>

#ifndef nullptr
#define nullptr 0
#endif

#ifndef MIN
#define MIN(A, B)        ((A) < (B) ? (A) : (B))
#endif


int ns_init(NSHandle_t *handle, int sample_rate, int channels, enum nsLevel level) {
	NsHandle **NsHandles;
	int16_t *frameBuffer;
    int samples = MIN(160, sample_rate / 100);

	ALOGD("%s()", __FUNCTION__);
	if(handle == NULL) return -1;
	handle->init_ok = 0;
	
    frameBuffer = (int16_t *) malloc(sizeof(*frameBuffer) * channels * samples);
    NsHandles = (NsHandle **) malloc(channels * sizeof(NsHandle *));
    if (NsHandles == NULL || frameBuffer == NULL) {
        if (NsHandles)
            free(NsHandles);
        if (frameBuffer)
            free(frameBuffer);
		ALOGE("%s() malloc error.", __FUNCTION__);
        return -1;
    }
    for (int i = 0; i < (int)channels; i++) {
        NsHandles[i] = WebRtcNs_Create();
        if (NsHandles[i] != NULL) {
            int status = WebRtcNs_Init(NsHandles[i], samples * 100);
            if (status != 0) {				
				ALOGE("%s() WebRtcNs_Init fail", __FUNCTION__);
                WebRtcNs_Free(NsHandles[i]);
                NsHandles[i] = NULL;
            } else {
                status = WebRtcNs_set_policy(NsHandles[i], level);
                if (status != 0) {
					ALOGE("%s() WebRtcNs_set_policy fail", __FUNCTION__);
                    WebRtcNs_Free(NsHandles[i]);
                    NsHandles[i] = NULL;
                }
            }
        }
        if (NsHandles[i] == NULL) {
            for (int x = 0; x < i; x++) {
                if (NsHandles[x]) {
                    WebRtcNs_Free(NsHandles[x]);
                }
            }
            free(NsHandles);
            free(frameBuffer);
			NsHandles = NULL;
			frameBuffer = NULL;
            return -1;
        }
    }
	
	handle->handles = NsHandles;
	handle->frame_buffer = frameBuffer;
	handle->channels = channels;
	handle->samples = samples;
	handle->init_ok = 1;
	
    return 0;
}

int ns_uninit(NSHandle_t *handle) {
	ALOGD("%s() handle->init_ok: %d", __FUNCTION__, handle->init_ok);

	if(handle == NULL) return -1;
	if(handle->init_ok == 0) {
		return 0;
	}

    for (int i = 0; i < (int)handle->channels; i++) {
        if (handle->handles[i]) {
            WebRtcNs_Free(handle->handles[i]);
        }
    }
    free(handle->handles);
    free(handle->frame_buffer);
	
	handle->handles = NULL;
	handle->frame_buffer = NULL;
	handle->channels = 0;
	handle->init_ok = 0;
	
	return 1;
}

int ns_process(NSHandle_t *handle, int16_t *buffer, uint64_t frames) {    
    uint32_t num_bands = 1;
    int16_t *input = buffer;

	if(handle == NULL) return -1;
	
	int channels = handle->channels;
	int samples = handle->samples;
	int16_t *frameBuffer = handle->frame_buffer;
	NsHandle **NsHandles = handle->handles;
	int count = 0;

	ALOGE("%s() handle->init_ok: %d, buffer = %p, frames: %d", __FUNCTION__, handle->init_ok, buffer, (int)frames);
	
	if (buffer == nullptr || handle->init_ok == 0) return -1;   	
	for (int i = 0; i < (int)frames; i++) {
        for (int c = 0; c < (int)channels; c++) {
            for (int k = 0; k < (int)samples; k++)
                frameBuffer[k] = input[k * channels + c];

            int16_t *nsIn[1] = {frameBuffer};   //ns input[band][data]
            int16_t *nsOut[1] = {frameBuffer};  //ns output[band][data]
            WebRtcNs_Analyze(NsHandles[c], nsIn[0]);
            WebRtcNs_Process(NsHandles[c], (const int16_t *const *) nsIn, num_bands, nsOut);

            for (int k = 0; k < (int)samples; k++)
                input[k * channels + c] = frameBuffer[k];
        }
		input += samples * channels;
    }
    return 1;
}
