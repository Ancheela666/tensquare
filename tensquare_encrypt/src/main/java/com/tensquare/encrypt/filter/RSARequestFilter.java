package com.tensquare.encrypt.filter;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import com.tensquare.encrypt.rsa.RsaKeys;
import com.tensquare.encrypt.service.RsaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RSARequestFilter extends ZuulFilter {

    @Autowired
    private RsaService rsaService;

    @Override
    public String filterType() { //过滤器在什么环节执行，解密操作需要在转发之前执行
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() { //设置过滤器的执行顺序
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
    }

    @Override
    public boolean shouldFilter() { //是否使用过滤器
        return true; //使用过滤器
    }

    @Override
    public Object run() throws ZuulException { //过滤器具体执行的逻辑
        System.out.println("过滤器执行了");
        //获取requestContext容器
        RequestContext ctx = RequestContext.getCurrentContext();

        //获取request和response
        HttpServletRequest request = ctx.getRequest();
        HttpServletResponse response = ctx.getResponse();

        //声明存放加密后的数据的变量
        String requestData = null;
        //声明存放解密后的数据变量
        String decryptData = null;

        try {
            ServletInputStream inputStream = request.getInputStream(); //通过request获取inputStream
            requestData = StreamUtils.copyToString(inputStream, Charsets.UTF_8); //从inputStream中得到加密后的数据
            //对加密后的数据进行解密操作
            if(!Strings.isNullOrEmpty(requestData)){
                decryptData = rsaService.RSADecryptDataPEM(requestData, RsaKeys.getServerPrvKeyPkcs8());
                System.out.println(decryptData);
            }
            //把解密后的数据进行转发，需要放到request中去
            if(!Strings.isNullOrEmpty(decryptData)){
                //获取解密后的数据的字节数组
                byte[] reqBodyBytes = decryptData.getBytes();
                //使用requestContext进行数据转发
                ctx.setRequest(new HttpServletRequestWrapper(request){

                    @Override
                    public ServletInputStream getInputStream() throws IOException {
                        return new ServletInputStreamWrapper(reqBodyBytes);
                    }

                    @Override
                    public int getContentLength() {
                        return reqBodyBytes.length;
                    }

                    @Override
                    public long getContentLengthLong() {
                        return reqBodyBytes.length;
                    }
                });
            }

            // 设置request请求头中的Content-Type为application/json，
            // 否则API接口模块需要进行url转码操作
            ctx.addZuulRequestHeader("Content-Type",
                    String.valueOf(MediaType.APPLICATION_JSON_VALUE) + ";charset=UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}