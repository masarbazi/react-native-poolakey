import { Text, View, StyleSheet, Button } from 'react-native';
import { useBazaar } from '@cafebazaar/react-native-poolakey';

export default function App() {
  const bazaar = useBazaar('rsa-here');

  const getPurchased = async () => {
    const result = await bazaar.getPurchasedProducts();
    console.log('result', result);
  };

  return (
    <View style={styles.container}>
      <Text>Result: TODO</Text>
      {/* Test connection */}
      <Button title="Purchase" onPress={getPurchased} />
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
