#Script to forecast half hourly request rate for spot scaling system
forecastEstimate <- function(v){
library(forecast)
library(zoo)
library(xts)
library(timetk)
print(v)

#reading the file using zoo function from the file specified using 'v' 
requestEstimate <- read.zoo(file=v,sep=",",header=FALSE,index=1:2,tz="UTC",format="%m/%d/%Y %H:%M")

#fitting a xts series using the index of requestEstimate
requestEstimateTs <- xts(requestEstimate,order.by=index(requestEstimate),tz="UTC")

#obtaining the proper arima model to be used as fit for the time series
modelCoeff <-  auto.arima(requestEstimateTs,approximation=FALSE)$arma
orderOfModel <- c(modelCoeff[1],modelCoeff[6],modelCoeff[2])
print(orderOfModel)

#fit an arima model
requestEstimateFit <- arima(requestEstimateTs,order=c(orderOfModel))
#forecasting with h=48
requestEstimateForecast <- forecast(requestEstimateFit,h=48,level=99)

#Transforming the output of forecast into meaningful form

indexOfSeries <- tk_index(requestEstimateTs)

#obtaining the index of future time frames using the input index with h=48
indexOfFutureSeries <- tk_make_future_timeseries(indexOfSeries, n_future = 48)

requestEstimatesFuture <- cbind(temp = requestEstimateForecast$mean, temp.lo = requestEstimateForecast$lower, temp.hi = requestEstimateForecast$upper)

requestEstimateFutureTs <- xts(requestEstimatesFuture,indexOfFutureSeries)

estimateFrame <- as.data.frame(requestEstimateFutureTs)

#Including the time because the previous frame does not give the time along with result

estimates <- as.data.frame(cbind(row.names(estimateFrame),estimateFrame$temp,estimateFrame$temp.lo,estimateFrame$temp.hi))

return (estimates)

}

