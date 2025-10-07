import { Text, View, StyleSheet, Button } from 'react-native';
import { useBazaar } from '@cafebazaar/react-native-poolakey';

export default function App() {
  const bazaar = useBazaar('rsa-here');

  const getPurchased = async () => {
    const result = await bazaar.getPurchasedProducts();
    console.log('result', result);
  };

  const purchaseProduct = async () => {
    try {
      const result = await bazaar.purchaseProduct('g10');
      console.log('purchase result', result);
    } catch (error) {
      console.error('purchase error', (error as Error).message);
    }
  };

  return (
    <View style={styles.container}>
      <Text>Result: TODO</Text>
      {/* Test connection */}
      <Button title="Get Purchased" onPress={getPurchased} />
      <Button title="Purchase" onPress={purchaseProduct} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#fcfcfc',
  },
});
