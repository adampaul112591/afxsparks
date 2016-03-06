package mtd.quant.afxsparks.portfolio;


import static mtd.quant.afxsparks.Constants.LONG_POSITION;
import static mtd.quant.afxsparks.Constants.SHORT_POSITION;
import static mtd.quant.afxsparks.MathUtils.roundHalfDown;
import static mtd.quant.afxsparks.MathUtils.roundHalfDownToPipDecimalPlaces;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mtd.quant.afxsparks.FXUtils;
import mtd.quant.afxsparks.data.Price;
import mtd.quant.afxsparks.data.StreamingForexPrices;

@Component
public class Position {

  private double averagePrice;

  private String baseCurrency;

  // market
  private String currencyPair;

  private double currentPrice;

  private double exposure;

  private String homeCurrency;

  // side
  private String positionType;

  @Autowired
  private StreamingForexPrices prices;

  private double profitBase;

  private double profitPercentage;

  private String quoteCurrency;

  private String quoteHomeCurrencyPair;

  private double units;

  public Position() {}

  public Position(String positionType, String currencyPair, double units, double exposure,
      double averagePrice, double currentPrice) {
    this.positionType = positionType;
    this.currencyPair = currencyPair;
    this.units = units;
    this.exposure = exposure;
    this.averagePrice = averagePrice;
    this.currentPrice = currentPrice;
    calculateProfitBase();
    calculateProfitPercentage();
  }

  public Position(String homeCurrency, String positionType, String currencyPair, double units) {
    this.homeCurrency = homeCurrency;
    this.positionType = positionType;
    this.currencyPair = currencyPair;
    this.units = units;
    setUpCurrencies();
    calculateProfitBase();
    calculateProfitPercentage();
  }

  public double getAveragePrice() {
    return averagePrice;
  }

  public String getBaseCurrency() {
    return baseCurrency;
  }

  public double getCurrentPrice() {
    return currentPrice;
  }

  public String getQuoteCurrency() {
    return quoteCurrency;
  }

  public String getQuoteHomeCurrencyPair() {
    return this.quoteCurrency;
  }

  public double calculatePips() {
    double mult = 1.0;
    if (positionType.equals(SHORT_POSITION)) {
      mult = -1.0;
    }
    return roundHalfDownToPipDecimalPlaces(mult * (currentPrice - averagePrice));
  }

  public void updatePositionPrice() {
    Price price = prices.getInstrumentPrice(currencyPair);
    if (positionType.equals(LONG_POSITION)) {
      this.currentPrice = price.getBid();
    } else {
      this.currentPrice = price.getAsk();
    }
    calculateProfitBase();
    calculateProfitPercentage();
  }

  public void updatePositionPrice(double currentPrice) {
    this.currentPrice = currentPrice;
    this.profitBase = calculateProfitBase();
    this.profitPercentage = calculateProfitPercentage();
  }

  private double calculateProfitBase() {
    double pips = calculatePips();
    double units = (exposure / currentPrice);
    double profitBase = pips * units;
    return roundHalfDownToPipDecimalPlaces(profitBase);
  }

  private double calculateProfitPercentage() {
    double profitPerc = (profitBase / exposure) * 100;
    return roundHalfDown(profitPerc, 2);
  }

  private void setUpCurrencies() {
    this.baseCurrency = FXUtils.extractBaseCurrency(currencyPair);
    this.quoteCurrency = FXUtils.extractQuoteCurrency(currencyPair);
    this.quoteHomeCurrencyPair = quoteCurrency + homeCurrency;

    Price price = prices.getInstrumentPrice(currencyPair);
    if (positionType.equals(LONG_POSITION)) {
      this.averagePrice = price.getAsk();
      this.currentPrice = price.getBid();
    } else {
      this.averagePrice = price.getBid();
      this.currentPrice = price.getAsk();
    }
  }

  public double getUnits() {
    return units;
  }

  public void setAveragePrice(double averagePrice) {
    this.averagePrice = averagePrice;
  }

  public void setUnits(double units) {
    this.units = units;
  }

  public double getProfitBase() {
    return profitBase;
  }

  public double getProfitPercentage() {
    return profitPercentage;
  }

  public double getExposure() {
    return exposure;
  }

  public void setExposure(double exposure) {
    this.exposure = exposure;
  }

  public String getCurrencyPair() {
    return this.currencyPair;
  }

  public String getPositionType() {
    return this.positionType;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != getClass()) {
      return false;
    } else if (obj == this) {
      return true;
    }

    Position position = (Position) obj;
    return new EqualsBuilder().append(position.getPositionType(), getPositionType())
        .append(position.getCurrencyPair(), getCurrencyPair())
        .append(position.getUnits(), getUnits()).append(position.getExposure(), getExposure())
        .append(position.getAveragePrice(), getAveragePrice())
        .append(position.getCurrentPrice(), getCurrentPrice())
        .append(position.getExposure(), getExposure())
        .append(position.getProfitBase(), getProfitBase())
        .append(position.getProfitPercentage(), getProfitPercentage()).isEquals();
  }

}
