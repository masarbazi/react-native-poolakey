import { TurboModuleRegistry, type TurboModule } from 'react-native';

export type SkuDetails = {
  sku: string;
  type: string;
  price: string;
  title: string;
  description: string;
};

type PurchaseState = 'PURCHASED' | 'REFUNDED';
export type PurchaseInfo = {
  orderId: string;
  purchaseToken: string;
  payload: string;
  packageName: string;
  purchaseState: PurchaseState;
  purchaseTime: number;
  productId: string;
  dataSignature: string;
};

export interface Spec extends TurboModule {
  connectPayment(rsa: string): Promise<void>;
  disconnectPayment(): void;
  purchaseProduct(
    productId: string,
    developerPayload?: string,
    dynamicPriceToken?: string
  ): Promise<PurchaseInfo>;
  subscribeProduct(
    productId: string,
    developerPayload?: string,
    dynamicPriceToken?: string
  ): Promise<PurchaseInfo>;
  consumePurchase(purchaseToken: string): Promise<void>;
  getPurchasedProducts(): Promise<PurchaseInfo[]>;
  getSubscribedProducts(): Promise<PurchaseInfo[]>;
  queryPurchaseProduct(productId: string): Promise<PurchaseInfo>;
  querySubscribeProduct(productId: string): Promise<PurchaseInfo>;
  getInAppSkuDetails(productIds: string[]): Promise<SkuDetails>;
  getSubscriptionSkuDetails(productIds: string[]): Promise<SkuDetails>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('ReactNativePoolakey');
